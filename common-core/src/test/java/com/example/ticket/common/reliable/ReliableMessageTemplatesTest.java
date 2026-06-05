package com.example.ticket.common.reliable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 可靠消息公共模板测试。
 * 用于锁定“先落任务再发消息”“失败转补发”“补发耗尽收敛”等公共行为，避免后续模板化时改坏现有链路。
 */
class ReliableMessageTemplatesTest {
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private InMemoryTaskStore taskStore;
    private RecordingMessageSender messageSender;
    private DemoReliablePublisher reliablePublisher;
    private DemoReliableRetryService reliableRetryService;

    /**
     * 构造每个测试使用的内存版依赖。
     * 这里不用数据库和 MQ，而是只验证模板层的行为约束。
     */
    @BeforeEach
    void setUp() {
        taskStore = new InMemoryTaskStore();
        messageSender = new RecordingMessageSender();
        reliablePublisher = new DemoReliablePublisher(taskStore, messageSender, objectMapper, 30, 3);
        reliableRetryService = new DemoReliableRetryService(taskStore, messageSender, objectMapper, 100, 30, 3);
    }

    /**
     * 首次发送成功时，模板应先写入待发送任务，再把任务收敛为已发送。
     */
    @Test
    void should_create_pending_task_and_mark_sent_when_publish_succeeds() {
        DemoEvent event = buildEvent();

        reliablePublisher.publish(event, LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        DemoTask storedTask = taskStore.mustFindById(1L);
        assertEquals("event-001", storedTask.getEventKey());
        assertEquals(ReliableMessageTaskStatus.SENT, storedTask.getTaskStatus());
        assertEquals(0, storedTask.getRetryCount());
        assertNotNull(storedTask.getLastSentAt());
        assertNull(storedTask.getLastErrorMessage());
        assertEquals("event-001", messageSender.getSentEvents().get(0).getEventId());
    }

    /**
     * 首次发送失败时，模板应保留补发事实，并进入补发中状态。
     */
    @Test
    void should_mark_task_retrying_when_publish_fails() {
        DemoEvent event = buildEvent();
        messageSender.setFailureMessage("mq unavailable");

        reliablePublisher.publish(event, LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        DemoTask storedTask = taskStore.mustFindById(1L);
        assertEquals(ReliableMessageTaskStatus.RETRYING, storedTask.getTaskStatus());
        assertEquals(1, storedTask.getRetryCount());
        assertEquals("mq unavailable", storedTask.getLastErrorMessage());
        assertEquals(LocalDateTime.of(2026, 6, 5, 20, 0, 30), storedTask.getNextRetryAt());
    }

    /**
     * 补发成功时，模板应把任务收敛为已发送，避免后续调度继续重复补发。
     */
    @Test
    void should_mark_task_sent_when_retry_succeeds() {
        DemoTask task = buildRetryingTask(2, 3, LocalDateTime.of(2026, 6, 5, 19, 0, 0));
        taskStore.insert(task);

        reliableRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        DemoTask storedTask = taskStore.mustFindById(task.getTaskId());
        assertEquals(ReliableMessageTaskStatus.SENT, storedTask.getTaskStatus());
        assertNotNull(storedTask.getLastSentAt());
        assertEquals("event-001", messageSender.getSentEvents().get(0).getEventId());
    }

    /**
     * 补发失败且未耗尽次数时，模板应累计重试次数并延后下一次补发窗口。
     */
    @Test
    void should_reschedule_retry_task_when_retry_fails_before_exhausted() {
        DemoTask task = buildRetryingTask(2, 4, LocalDateTime.of(2026, 6, 5, 19, 0, 0));
        taskStore.insert(task);
        messageSender.setFailureMessage("mq timeout");

        reliableRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        DemoTask storedTask = taskStore.mustFindById(task.getTaskId());
        assertEquals(ReliableMessageTaskStatus.RETRYING, storedTask.getTaskStatus());
        assertEquals(3, storedTask.getRetryCount());
        assertEquals(LocalDateTime.of(2026, 6, 5, 20, 0, 30), storedTask.getNextRetryAt());
        assertEquals("mq timeout", storedTask.getLastErrorMessage());
    }

    /**
     * 补发失败且达到上限时，模板应把任务收敛为耗尽，避免无限重试。
     */
    @Test
    void should_mark_task_exhausted_when_retry_count_reaches_limit() {
        DemoTask task = buildRetryingTask(2, 3, LocalDateTime.of(2026, 6, 5, 19, 0, 0));
        taskStore.insert(task);
        messageSender.setFailureMessage("mq timeout");

        reliableRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        DemoTask storedTask = taskStore.mustFindById(task.getTaskId());
        assertEquals(ReliableMessageTaskStatus.EXHAUSTED, storedTask.getTaskStatus());
        assertEquals(3, storedTask.getRetryCount());
        assertNull(storedTask.getNextRetryAt());
    }

    /**
     * 构造演示事件。
     *
     * @return 用于模板测试的演示事件
     */
    private DemoEvent buildEvent() {
        DemoEvent event = new DemoEvent();
        event.setEventId("event-001");
        event.setEventType("EVENT_CREATED");
        event.setBusinessKey("biz-001");
        return event;
    }

    /**
     * 构造待补发任务。
     *
     * @param retryCount 当前已重试次数
     * @param maxRetryCount 最大重试次数
     * @param nextRetryAt 下一次补发时间
     * @return 演示补发任务
     */
    private DemoTask buildRetryingTask(int retryCount, int maxRetryCount, LocalDateTime nextRetryAt) {
        DemoTask task = new DemoTask();
        task.setEventKey("event-001");
        task.setEventType("EVENT_CREATED");
        task.setBusinessKey("biz-001");
        task.setPayloadJson("{\"eventId\":\"event-001\",\"eventType\":\"EVENT_CREATED\",\"businessKey\":\"biz-001\"}");
        task.setTaskStatus(ReliableMessageTaskStatus.RETRYING);
        task.setRetryCount(retryCount);
        task.setMaxRetryCount(maxRetryCount);
        task.setNextRetryAt(nextRetryAt);
        return task;
    }

    /**
     * 演示事件。
     * 用于验证模板对事件关键信息的抽取与序列化行为。
     */
    static class DemoEvent {
        private String eventId;
        private String eventType;
        private String businessKey;

        /**
         * 获取事件标识。
         *
         * @return 事件标识
         */
        public String getEventId() {
            return eventId;
        }

        /**
         * 设置事件标识。
         *
         * @param eventId 事件标识
         */
        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        /**
         * 获取事件类型。
         *
         * @return 事件类型
         */
        public String getEventType() {
            return eventType;
        }

        /**
         * 设置事件类型。
         *
         * @param eventType 事件类型
         */
        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        /**
         * 获取业务主键。
         *
         * @return 业务主键
         */
        public String getBusinessKey() {
            return businessKey;
        }

        /**
         * 设置业务主键。
         *
         * @param businessKey 业务主键
         */
        public void setBusinessKey(String businessKey) {
            this.businessKey = businessKey;
        }
    }

    /**
     * 演示任务对象。
     * 用于模拟不同业务表映射到统一可靠消息任务视图后的公共字段。
     */
    static class DemoTask implements ReliableMessageTask {
        private Long taskId;
        private String eventKey;
        private String eventType;
        private String businessKey;
        private String payloadJson;
        private String taskStatus;
        private Integer retryCount;
        private Integer maxRetryCount;
        private LocalDateTime nextRetryAt;
        private LocalDateTime lastSentAt;
        private String lastErrorMessage;

        /**
         * 获取任务主键。
         *
         * @return 任务主键
         */
        @Override
        public Long getTaskId() {
            return taskId;
        }

        /**
         * 设置任务主键。
         *
         * @param taskId 任务主键
         */
        @Override
        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        /**
         * 获取统一事件键。
         *
         * @return 统一事件键
         */
        @Override
        public String getEventKey() {
            return eventKey;
        }

        /**
         * 设置统一事件键。
         *
         * @param eventKey 统一事件键
         */
        @Override
        public void setEventKey(String eventKey) {
            this.eventKey = eventKey;
        }

        /**
         * 获取事件类型。
         *
         * @return 事件类型
         */
        @Override
        public String getEventType() {
            return eventType;
        }

        /**
         * 设置事件类型。
         *
         * @param eventType 事件类型
         */
        @Override
        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        /**
         * 获取业务主键。
         *
         * @return 业务主键
         */
        @Override
        public String getBusinessKey() {
            return businessKey;
        }

        /**
         * 设置业务主键。
         *
         * @param businessKey 业务主键
         */
        @Override
        public void setBusinessKey(String businessKey) {
            this.businessKey = businessKey;
        }

        /**
         * 获取事件载荷。
         *
         * @return 事件载荷
         */
        @Override
        public String getPayloadJson() {
            return payloadJson;
        }

        /**
         * 设置事件载荷。
         *
         * @param payloadJson 事件载荷
         */
        @Override
        public void setPayloadJson(String payloadJson) {
            this.payloadJson = payloadJson;
        }

        /**
         * 获取任务状态。
         *
         * @return 任务状态
         */
        @Override
        public String getTaskStatus() {
            return taskStatus;
        }

        /**
         * 设置任务状态。
         *
         * @param taskStatus 任务状态
         */
        @Override
        public void setTaskStatus(String taskStatus) {
            this.taskStatus = taskStatus;
        }

        /**
         * 获取重试次数。
         *
         * @return 重试次数
         */
        @Override
        public Integer getRetryCount() {
            return retryCount;
        }

        /**
         * 设置重试次数。
         *
         * @param retryCount 重试次数
         */
        @Override
        public void setRetryCount(Integer retryCount) {
            this.retryCount = retryCount;
        }

        /**
         * 获取最大重试次数。
         *
         * @return 最大重试次数
         */
        @Override
        public Integer getMaxRetryCount() {
            return maxRetryCount;
        }

        /**
         * 设置最大重试次数。
         *
         * @param maxRetryCount 最大重试次数
         */
        @Override
        public void setMaxRetryCount(Integer maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }

        /**
         * 获取下一次补发时间。
         *
         * @return 下一次补发时间
         */
        @Override
        public LocalDateTime getNextRetryAt() {
            return nextRetryAt;
        }

        /**
         * 设置下一次补发时间。
         *
         * @param nextRetryAt 下一次补发时间
         */
        @Override
        public void setNextRetryAt(LocalDateTime nextRetryAt) {
            this.nextRetryAt = nextRetryAt;
        }

        /**
         * 获取最后一次发送成功时间。
         *
         * @return 最后一次发送成功时间
         */
        @Override
        public LocalDateTime getLastSentAt() {
            return lastSentAt;
        }

        /**
         * 设置最后一次发送成功时间。
         *
         * @param lastSentAt 最后一次发送成功时间
         */
        @Override
        public void setLastSentAt(LocalDateTime lastSentAt) {
            this.lastSentAt = lastSentAt;
        }

        /**
         * 获取最后一次错误信息。
         *
         * @return 最后一次错误信息
         */
        @Override
        public String getLastErrorMessage() {
            return lastErrorMessage;
        }

        /**
         * 设置最后一次错误信息。
         *
         * @param lastErrorMessage 最后一次错误信息
         */
        @Override
        public void setLastErrorMessage(String lastErrorMessage) {
            this.lastErrorMessage = lastErrorMessage;
        }
    }

    /**
     * 演示发送器。
     * 用于记录已发送事件，并按测试需要模拟 MQ 发送失败。
     */
    static class RecordingMessageSender implements ReliableMessageSender<DemoEvent> {
        private final List<DemoEvent> sentEvents = new ArrayList<>();
        private String failureMessage;

        /**
         * 执行消息发送。
         *
         * @param event 待发送事件
         */
        @Override
        public void send(DemoEvent event) {
            if (failureMessage != null) {
                throw new IllegalStateException(failureMessage);
            }
            sentEvents.add(event);
        }

        /**
         * 设置下一次发送失败信息。
         *
         * @param failureMessage 失败信息
         */
        public void setFailureMessage(String failureMessage) {
            this.failureMessage = failureMessage;
        }

        /**
         * 获取已发送事件列表。
         *
         * @return 已发送事件列表
         */
        public List<DemoEvent> getSentEvents() {
            return sentEvents;
        }
    }

    /**
     * 内存版任务存储。
     * 用于替代数据库，验证模板如何操作统一任务字段。
     */
    static class InMemoryTaskStore implements ReliableMessageTaskStore<DemoTask> {
        private final List<DemoTask> tasks = new ArrayList<>();
        private long nextTaskId = 1L;

        /**
         * 插入新任务。
         *
         * @param task 待插入任务
         */
        @Override
        public void insert(DemoTask task) {
            if (task.getTaskId() == null) {
                task.setTaskId(nextTaskId++);
            }
            tasks.add(copy(task));
        }

        /**
         * 按主键更新任务。
         *
         * @param task 仅包含增量字段的更新对象
         */
        @Override
        public void updateById(DemoTask task) {
            DemoTask storedTask = mustFindById(task.getTaskId());
            if (task.getEventKey() != null) {
                storedTask.setEventKey(task.getEventKey());
            }
            if (task.getEventType() != null) {
                storedTask.setEventType(task.getEventType());
            }
            if (task.getBusinessKey() != null) {
                storedTask.setBusinessKey(task.getBusinessKey());
            }
            if (task.getPayloadJson() != null) {
                storedTask.setPayloadJson(task.getPayloadJson());
            }
            if (task.getTaskStatus() != null) {
                storedTask.setTaskStatus(task.getTaskStatus());
            }
            if (task.getRetryCount() != null) {
                storedTask.setRetryCount(task.getRetryCount());
            }
            if (task.getMaxRetryCount() != null) {
                storedTask.setMaxRetryCount(task.getMaxRetryCount());
            }
            storedTask.setNextRetryAt(task.getNextRetryAt());
            storedTask.setLastSentAt(task.getLastSentAt());
            storedTask.setLastErrorMessage(task.getLastErrorMessage());
        }

        /**
         * 加载到期任务。
         *
         * @param taskStatuses 允许被补发的状态集合
         * @param currentTime 当前扫描时间
         * @param batchSize 单次批量大小
         * @return 到期任务列表
         */
        @Override
        public List<DemoTask> loadDueTasks(List<String> taskStatuses, LocalDateTime currentTime, int batchSize) {
            return tasks.stream()
                    .filter(task -> taskStatuses.contains(task.getTaskStatus()))
                    .filter(task -> task.getNextRetryAt() != null && !task.getNextRetryAt().isAfter(currentTime))
                    .sorted(Comparator.comparing(DemoTask::getNextRetryAt))
                    .limit(batchSize)
                    .map(this::copy)
                    .collect(Collectors.toList());
        }

        /**
         * 按主键获取任务。
         *
         * @param taskId 任务主键
         * @return 任务对象
         */
        public DemoTask mustFindById(Long taskId) {
            return tasks.stream()
                    .filter(task -> Objects.equals(task.getTaskId(), taskId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("task not found: " + taskId));
        }

        /**
         * 复制任务对象。
         * 这样可以避免测试误把内存引用共享当成模板行为。
         *
         * @param source 原始任务
         * @return 复制后的任务
         */
        private DemoTask copy(DemoTask source) {
            DemoTask copy = new DemoTask();
            copy.setTaskId(source.getTaskId());
            copy.setEventKey(source.getEventKey());
            copy.setEventType(source.getEventType());
            copy.setBusinessKey(source.getBusinessKey());
            copy.setPayloadJson(source.getPayloadJson());
            copy.setTaskStatus(source.getTaskStatus());
            copy.setRetryCount(source.getRetryCount());
            copy.setMaxRetryCount(source.getMaxRetryCount());
            copy.setNextRetryAt(source.getNextRetryAt());
            copy.setLastSentAt(source.getLastSentAt());
            copy.setLastErrorMessage(source.getLastErrorMessage());
            return copy;
        }
    }

    /**
     * 演示可靠发布器。
     * 用于把业务事件适配到公共可靠消息发布模板。
     */
    static class DemoReliablePublisher extends AbstractReliableMessagePublisher<DemoEvent, DemoTask> {
        /**
         * 构造演示可靠发布器。
         *
         * @param taskStore 任务存储
         * @param messageSender 消息发送器
         * @param objectMapper JSON 工具
         * @param retryIntervalSeconds 失败后补发间隔
         * @param maxRetryCount 最大重试次数
         */
        DemoReliablePublisher(
                ReliableMessageTaskStore<DemoTask> taskStore,
                ReliableMessageSender<DemoEvent> messageSender,
                ObjectMapper objectMapper,
                int retryIntervalSeconds,
                int maxRetryCount
        ) {
            super(taskStore, messageSender, objectMapper, retryIntervalSeconds, maxRetryCount);
        }

        /**
         * 创建空任务对象。
         *
         * @return 新任务对象
         */
        @Override
        protected DemoTask createTask() {
            return new DemoTask();
        }

        /**
         * 提取统一事件键。
         *
         * @param event 演示事件
         * @return 统一事件键
         */
        @Override
        protected String extractEventKey(DemoEvent event) {
            return event.getEventId();
        }

        /**
         * 提取事件类型。
         *
         * @param event 演示事件
         * @return 事件类型
         */
        @Override
        protected String extractEventType(DemoEvent event) {
            return event.getEventType();
        }

        /**
         * 提取业务主键。
         *
         * @param event 演示事件
         * @return 业务主键
         */
        @Override
        protected String extractBusinessKey(DemoEvent event) {
            return event.getBusinessKey();
        }
    }

    /**
     * 演示补发服务。
     * 用于把业务任务扫描与统一可靠消息补发模板连接起来。
     */
    static class DemoReliableRetryService extends AbstractReliableMessageRetryService<DemoEvent, DemoTask> {
        /**
         * 构造演示补发服务。
         *
         * @param taskStore 任务存储
         * @param messageSender 消息发送器
         * @param objectMapper JSON 工具
         * @param retryBatchSize 单次扫描批量大小
         * @param retryIntervalSeconds 失败后补发间隔
         * @param defaultMaxRetryCount 默认最大重试次数
         */
        DemoReliableRetryService(
                ReliableMessageTaskStore<DemoTask> taskStore,
                ReliableMessageSender<DemoEvent> messageSender,
                ObjectMapper objectMapper,
                int retryBatchSize,
                int retryIntervalSeconds,
                int defaultMaxRetryCount
        ) {
            super(taskStore, messageSender, objectMapper, DemoEvent.class, retryBatchSize, retryIntervalSeconds,
                    defaultMaxRetryCount);
        }

        /**
         * 创建空任务对象。
         *
         * @return 新任务对象
         */
        @Override
        protected DemoTask createTask() {
            return new DemoTask();
        }
    }
}
