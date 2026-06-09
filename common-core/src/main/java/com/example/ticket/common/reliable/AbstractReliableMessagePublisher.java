package com.example.ticket.common.reliable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * 可靠消息发布模板。
 * 用于统一“先落补偿任务，再尝试发消息，失败后转入补发状态”的行为，降低多业务域重复实现成本。
 *
 * @param <TEvent> 事件类型
 * @param <TTask> 任务类型
 */
public abstract class AbstractReliableMessagePublisher<TEvent, TTask extends ReliableMessageTask> {
    private static final Logger log = LoggerFactory.getLogger(AbstractReliableMessagePublisher.class);

    private final ReliableMessageTaskStore<TTask> taskStore;
    private final ReliableMessageSender<TEvent> messageSender;
    private final ObjectMapper objectMapper;
    private final int retryIntervalSeconds;
    private final int maxRetryCount;

    /**
     * 构造可靠消息发布模板。
     *
     * @param taskStore 任务存储
     * @param messageSender 底层消息发送器
     * @param objectMapper JSON 工具
     * @param retryIntervalSeconds 首次发送失败后的重试间隔
     * @param maxRetryCount 最大重试次数
     */
    protected AbstractReliableMessagePublisher(
            ReliableMessageTaskStore<TTask> taskStore,
            ReliableMessageSender<TEvent> messageSender,
            ObjectMapper objectMapper,
            int retryIntervalSeconds,
            int maxRetryCount
    ) {
        this.taskStore = taskStore;
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
        this.retryIntervalSeconds = retryIntervalSeconds;
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * 使用当前时间发布事件。
     *
     * @param event 待发布事件
     */
    public void publish(TEvent event) {
        publish(event, LocalDateTime.now());
    }

    /**
     * 使用指定时间发布事件。
     * 这个重载主要服务于测试和可控调度场景，确保时间相关断言稳定可测。
     *
     * @param event 待发布事件
     * @param currentTime 当前时间
     */
    public void publish(TEvent event, LocalDateTime currentTime) {
        TTask task = buildPendingTask(event, currentTime);
        taskStore.insert(task);
        log.info(
                "可靠消息任务已落库，taskId={}, eventType={}, eventKey={}, businessKey={}, nextRetryAt={}",
                task.getTaskId(),
                task.getEventType(),
                task.getEventKey(),
                task.getBusinessKey(),
                task.getNextRetryAt()
        );
        try {
            messageSender.send(event);
            markTaskSent(task.getTaskId(), currentTime);
            log.info(
                    "可靠消息首次发送成功，taskId={}, eventType={}, eventKey={}, businessKey={}, sentAt={}",
                    task.getTaskId(),
                    task.getEventType(),
                    task.getEventKey(),
                    task.getBusinessKey(),
                    currentTime
            );
        } catch (RuntimeException exception) {
            markTaskRetrying(task.getTaskId(), currentTime, exception);
            log.warn(
                    "可靠消息首次发送失败，已转入补发状态，taskId={}, eventType={}, eventKey={}, businessKey={}, nextRetryAt={}, errorMessage={}",
                    task.getTaskId(),
                    task.getEventType(),
                    task.getEventKey(),
                    task.getBusinessKey(),
                    currentTime.plusSeconds(retryIntervalSeconds),
                    exception.getMessage()
            );
        }
    }

    /**
     * 构造新的任务对象。
     *
     * @return 空任务对象
     */
    protected abstract TTask createTask();

    /**
     * 从事件中提取统一事件键。
     *
     * @param event 业务事件
     * @return 统一事件键
     */
    protected abstract String extractEventKey(TEvent event);

    /**
     * 从事件中提取事件类型。
     *
     * @param event 业务事件
     * @return 事件类型
     */
    protected abstract String extractEventType(TEvent event);

    /**
     * 从事件中提取业务主键。
     *
     * @param event 业务事件
     * @return 业务主键
     */
    protected abstract String extractBusinessKey(TEvent event);

    /**
     * 构造待发送任务。
     * 这里统一把补偿任务初始化为 `PENDING`，确保所有业务域的首次发送语义一致。
     *
     * @param event 业务事件
     * @param currentTime 当前时间
     * @return 待发送任务
     */
    protected TTask buildPendingTask(TEvent event, LocalDateTime currentTime) {
        TTask task = createTask();
        task.setEventKey(extractEventKey(event));
        task.setEventType(extractEventType(event));
        task.setBusinessKey(extractBusinessKey(event));
        task.setPayloadJson(serializeEvent(event));
        task.setTaskStatus(ReliableMessageTaskStatus.PENDING);
        task.setRetryCount(0);
        task.setMaxRetryCount(maxRetryCount);
        task.setNextRetryAt(currentTime);
        return task;
    }

    /**
     * 序列化事件载荷。
     *
     * @param event 业务事件
     * @return 事件 JSON
     */
    protected String serializeEvent(TEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("可靠消息事件载荷序列化失败", exception);
        }
    }

    /**
     * 把发送成功的任务收敛为已发送。
     *
     * @param taskId 任务主键
     * @param currentTime 当前时间
     */
    protected void markTaskSent(Long taskId, LocalDateTime currentTime) {
        TTask updateTarget = createTask();
        updateTarget.setTaskId(taskId);
        updateTarget.setTaskStatus(ReliableMessageTaskStatus.SENT);
        updateTarget.setLastSentAt(currentTime);
        updateTarget.setLastErrorMessage(null);
        taskStore.updateById(updateTarget);
    }

    /**
     * 记录首次发送失败。
     * 这里不向上抛出异常，而是显式留下可补发事实，避免消息静默丢失。
     *
     * @param taskId 任务主键
     * @param currentTime 当前时间
     * @param exception 发送异常
     */
    protected void markTaskRetrying(Long taskId, LocalDateTime currentTime, RuntimeException exception) {
        TTask updateTarget = createTask();
        updateTarget.setTaskId(taskId);
        updateTarget.setTaskStatus(ReliableMessageTaskStatus.RETRYING);
        updateTarget.setRetryCount(1);
        updateTarget.setLastErrorMessage(exception.getMessage());
        updateTarget.setNextRetryAt(currentTime.plusSeconds(retryIntervalSeconds));
        taskStore.updateById(updateTarget);
    }
}
