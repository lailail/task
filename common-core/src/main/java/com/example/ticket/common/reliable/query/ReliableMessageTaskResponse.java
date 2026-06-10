package com.example.ticket.common.reliable.query;

import java.time.LocalDateTime;

/**
 * 补偿任务统一响应对象。
 * 用于向后台治理页暴露跨服务稳定一致的补偿任务视图，避免前端感知各物理任务表差异。
 */
public class ReliableMessageTaskResponse {
    private Long taskId;
    private String taskType;
    private String eventKey;
    private String eventType;
    private String businessKey;
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
    public Long getTaskId() {
        return taskId;
    }

    /**
     * 设置任务主键。
     *
     * @param taskId 任务主键
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * 获取任务类型。
     *
     * @return 任务类型
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * 设置任务类型。
     *
     * @param taskType 任务类型
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    /**
     * 获取事件键。
     *
     * @return 事件键
     */
    public String getEventKey() {
        return eventKey;
    }

    /**
     * 设置事件键。
     *
     * @param eventKey 事件键
     */
    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
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

    /**
     * 获取任务状态。
     *
     * @return 任务状态
     */
    public String getTaskStatus() {
        return taskStatus;
    }

    /**
     * 设置任务状态。
     *
     * @param taskStatus 任务状态
     */
    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    /**
     * 获取已执行重试次数。
     *
     * @return 已执行重试次数
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 设置已执行重试次数。
     *
     * @param retryCount 已执行重试次数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 获取最大允许重试次数。
     *
     * @return 最大允许重试次数
     */
    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * 设置最大允许重试次数。
     *
     * @param maxRetryCount 最大允许重试次数
     */
    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * 获取下次重试时间。
     *
     * @return 下次重试时间
     */
    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    /**
     * 设置下次重试时间。
     *
     * @param nextRetryAt 下次重试时间
     */
    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    /**
     * 获取最后一次成功发送时间。
     *
     * @return 最后一次成功发送时间
     */
    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    /**
     * 设置最后一次成功发送时间。
     *
     * @param lastSentAt 最后一次成功发送时间
     */
    public void setLastSentAt(LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }

    /**
     * 获取最近一次失败原因。
     *
     * @return 最近一次失败原因
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * 设置最近一次失败原因。
     *
     * @param lastErrorMessage 最近一次失败原因
     */
    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
}
