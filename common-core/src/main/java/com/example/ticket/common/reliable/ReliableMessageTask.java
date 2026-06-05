package com.example.ticket.common.reliable;

import java.time.LocalDateTime;

/**
 * 可靠消息任务统一视图。
 * 用于在不合并物理表的前提下，为不同业务任务表暴露一致的补偿字段读写能力。
 */
public interface ReliableMessageTask {
    /**
     * 获取任务主键。
     *
     * @return 任务主键
     */
    Long getTaskId();

    /**
     * 设置任务主键。
     *
     * @param taskId 任务主键
     */
    void setTaskId(Long taskId);

    /**
     * 获取统一事件键。
     * 这里不强制要求底层字段名一致，`event_id`、`event_key` 都通过这个视图收敛。
     *
     * @return 统一事件键
     */
    String getEventKey();

    /**
     * 设置统一事件键。
     *
     * @param eventKey 统一事件键
     */
    void setEventKey(String eventKey);

    /**
     * 获取事件类型。
     *
     * @return 事件类型
     */
    String getEventType();

    /**
     * 设置事件类型。
     *
     * @param eventType 事件类型
     */
    void setEventType(String eventType);

    /**
     * 获取业务主键。
     *
     * @return 业务主键
     */
    String getBusinessKey();

    /**
     * 设置业务主键。
     *
     * @param businessKey 业务主键
     */
    void setBusinessKey(String businessKey);

    /**
     * 获取事件载荷。
     *
     * @return 事件载荷 JSON
     */
    String getPayloadJson();

    /**
     * 设置事件载荷。
     *
     * @param payloadJson 事件载荷 JSON
     */
    void setPayloadJson(String payloadJson);

    /**
     * 获取任务状态。
     *
     * @return 任务状态
     */
    String getTaskStatus();

    /**
     * 设置任务状态。
     *
     * @param taskStatus 任务状态
     */
    void setTaskStatus(String taskStatus);

    /**
     * 获取当前重试次数。
     *
     * @return 当前重试次数
     */
    Integer getRetryCount();

    /**
     * 设置当前重试次数。
     *
     * @param retryCount 当前重试次数
     */
    void setRetryCount(Integer retryCount);

    /**
     * 获取最大重试次数。
     *
     * @return 最大重试次数
     */
    Integer getMaxRetryCount();

    /**
     * 设置最大重试次数。
     *
     * @param maxRetryCount 最大重试次数
     */
    void setMaxRetryCount(Integer maxRetryCount);

    /**
     * 获取下一次补发时间。
     *
     * @return 下一次补发时间
     */
    LocalDateTime getNextRetryAt();

    /**
     * 设置下一次补发时间。
     *
     * @param nextRetryAt 下一次补发时间
     */
    void setNextRetryAt(LocalDateTime nextRetryAt);

    /**
     * 获取最后一次发送成功时间。
     *
     * @return 最后一次发送成功时间
     */
    LocalDateTime getLastSentAt();

    /**
     * 设置最后一次发送成功时间。
     *
     * @param lastSentAt 最后一次发送成功时间
     */
    void setLastSentAt(LocalDateTime lastSentAt);

    /**
     * 获取最后一次错误信息。
     *
     * @return 最后一次错误信息
     */
    String getLastErrorMessage();

    /**
     * 设置最后一次错误信息。
     *
     * @param lastErrorMessage 最后一次错误信息
     */
    void setLastErrorMessage(String lastErrorMessage);
}
