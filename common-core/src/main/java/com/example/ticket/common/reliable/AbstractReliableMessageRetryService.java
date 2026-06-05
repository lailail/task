package com.example.ticket.common.reliable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 可靠消息补发模板。
 * 用于统一“扫描到期任务、重新发送、成功收敛、失败延后或耗尽”的补偿逻辑。
 *
 * @param <TEvent> 事件类型
 * @param <TTask> 任务类型
 */
public abstract class AbstractReliableMessageRetryService<TEvent, TTask extends ReliableMessageTask> {
    private final ReliableMessageTaskStore<TTask> taskStore;
    private final ReliableMessageSender<TEvent> messageSender;
    private final ObjectMapper objectMapper;
    private final Class<TEvent> eventClass;
    private final int retryBatchSize;
    private final int retryIntervalSeconds;
    private final int defaultMaxRetryCount;

    /**
     * 构造可靠消息补发模板。
     *
     * @param taskStore 任务存储
     * @param messageSender 底层消息发送器
     * @param objectMapper JSON 工具
     * @param eventClass 事件类型
     * @param retryBatchSize 单次扫描批量大小
     * @param retryIntervalSeconds 失败后的补发间隔
     * @param defaultMaxRetryCount 默认最大重试次数
     */
    protected AbstractReliableMessageRetryService(
            ReliableMessageTaskStore<TTask> taskStore,
            ReliableMessageSender<TEvent> messageSender,
            ObjectMapper objectMapper,
            Class<TEvent> eventClass,
            int retryBatchSize,
            int retryIntervalSeconds,
            int defaultMaxRetryCount
    ) {
        this.taskStore = taskStore;
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
        this.eventClass = eventClass;
        this.retryBatchSize = retryBatchSize;
        this.retryIntervalSeconds = retryIntervalSeconds;
        this.defaultMaxRetryCount = defaultMaxRetryCount;
    }

    /**
     * 使用当前时间执行一次补发扫描。
     */
    public void retryDueTasks() {
        retryDueTasks(LocalDateTime.now());
    }

    /**
     * 使用指定时间执行一次补发扫描。
     *
     * @param currentTime 当前扫描时间
     */
    public void retryDueTasks(LocalDateTime currentTime) {
        List<TTask> dueTasks = taskStore.loadDueTasks(
                List.of(ReliableMessageTaskStatus.PENDING, ReliableMessageTaskStatus.RETRYING),
                currentTime,
                retryBatchSize
        );
        for (TTask dueTask : dueTasks) {
            retrySingleTask(dueTask, currentTime);
        }
    }

    /**
     * 创建新的任务对象。
     * 这个钩子用于构造增量更新对象，避免模板层依赖具体 DO 构造方式。
     *
     * @return 空任务对象
     */
    protected abstract TTask createTask();

    /**
     * 补发单条任务。
     *
     * @param task 到期任务
     * @param currentTime 当前扫描时间
     */
    protected void retrySingleTask(TTask task, LocalDateTime currentTime) {
        try {
            messageSender.send(deserializeEvent(task.getPayloadJson()));
            markSent(task.getTaskId(), currentTime);
        } catch (RuntimeException exception) {
            markRetryFailed(task, currentTime, exception);
        }
    }

    /**
     * 反序列化事件载荷。
     *
     * @param payloadJson 事件 JSON
     * @return 事件对象
     */
    protected TEvent deserializeEvent(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, eventClass);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("可靠消息事件载荷反序列化失败", exception);
        }
    }

    /**
     * 把补发成功的任务收敛为已发送。
     *
     * @param taskId 任务主键
     * @param currentTime 当前时间
     */
    protected void markSent(Long taskId, LocalDateTime currentTime) {
        TTask updateTarget = createTask();
        updateTarget.setTaskId(taskId);
        updateTarget.setTaskStatus(ReliableMessageTaskStatus.SENT);
        updateTarget.setLastSentAt(currentTime);
        updateTarget.setLastErrorMessage(null);
        taskStore.updateById(updateTarget);
    }

    /**
     * 记录补发失败结果。
     * 当任务达到最大重试次数时，模板会显式转为 `EXHAUSTED`，避免无限补发。
     *
     * @param task 原任务
     * @param currentTime 当前时间
     * @param exception 发送异常
     */
    protected void markRetryFailed(TTask task, LocalDateTime currentTime, RuntimeException exception) {
        int nextRetryCount = safeRetryCount(task.getRetryCount()) + 1;
        TTask updateTarget = createTask();
        updateTarget.setTaskId(task.getTaskId());
        updateTarget.setRetryCount(nextRetryCount);
        updateTarget.setLastErrorMessage(exception.getMessage());

        if (nextRetryCount >= safeMaxRetryCount(task.getMaxRetryCount())) {
            updateTarget.setTaskStatus(ReliableMessageTaskStatus.EXHAUSTED);
            updateTarget.setNextRetryAt(null);
        } else {
            updateTarget.setTaskStatus(ReliableMessageTaskStatus.RETRYING);
            updateTarget.setNextRetryAt(currentTime.plusSeconds(retryIntervalSeconds));
        }
        taskStore.updateById(updateTarget);
    }

    /**
     * 获取安全的当前重试次数。
     *
     * @param retryCount 原始重试次数
     * @return 安全重试次数
     */
    protected int safeRetryCount(Integer retryCount) {
        return retryCount == null ? 0 : retryCount;
    }

    /**
     * 获取安全的最大重试次数。
     * 这里优先使用任务自带上限，只有历史脏数据为空时才退回模板默认值。
     *
     * @param maxRetryCount 原始最大重试次数
     * @return 安全最大重试次数
     */
    protected int safeMaxRetryCount(Integer maxRetryCount) {
        return maxRetryCount == null ? defaultMaxRetryCount : maxRetryCount;
    }
}
