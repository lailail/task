package com.example.ticket.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ticket.common.reliable.ReliableMessageTask;

import java.time.LocalDateTime;

/**
 * 库存释放补偿任务持久化对象。
 * 用于映射 `stock_release_task` 表，并通过统一任务视图接入公共可靠消息模板。
 */
@TableName("stock_release_task")
public class JobStockReleaseTaskDO implements ReliableMessageTask {
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    @TableField("event_id")
    private String eventId;

    @TableField("event_type")
    private String eventType;

    @TableField("business_key")
    private String businessKey;

    @TableField("payload_json")
    private String payloadJson;

    @TableField("task_status")
    private String taskStatus;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("max_retry_count")
    private Integer maxRetryCount;

    @TableField("next_retry_at")
    private LocalDateTime nextRetryAt;

    @TableField("last_sent_at")
    private LocalDateTime lastSentAt;

    @TableField("last_error_message")
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
     * 获取原始事件标识字段。
     *
     * @return 原始事件标识字段
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 设置原始事件标识字段。
     *
     * @param eventId 原始事件标识字段
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 获取统一事件键。
     * 这里把公共模板需要的统一事件键映射到底层 `event_id` 字段，避免改动现有表结构。
     *
     * @return 统一事件键
     */
    @Override
    public String getEventKey() {
        return eventId;
    }

    /**
     * 设置统一事件键。
     * 这里把公共模板写入统一事件键时收敛到底层 `event_id` 字段。
     *
     * @param eventKey 统一事件键
     */
    @Override
    public void setEventKey(String eventKey) {
        this.eventId = eventKey;
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
     * 获取事件载荷 JSON。
     *
     * @return 事件载荷 JSON
     */
    @Override
    public String getPayloadJson() {
        return payloadJson;
    }

    /**
     * 设置事件载荷 JSON。
     *
     * @param payloadJson 事件载荷 JSON
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
     * 获取当前重试次数。
     *
     * @return 当前重试次数
     */
    @Override
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 设置当前重试次数。
     *
     * @param retryCount 当前重试次数
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
