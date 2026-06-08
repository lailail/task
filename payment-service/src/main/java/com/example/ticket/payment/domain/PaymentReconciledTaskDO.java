package com.example.ticket.payment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ticket.common.reliable.ReliableMessageTask;

import java.time.LocalDateTime;

/**
 * 支付收敛事件补偿任务持久化对象。
 * 用于映射 `payment_reconciled_task` 表，并接入统一可靠消息模板。
 */
@TableName("payment_reconciled_task")
public class PaymentReconciledTaskDO implements ReliableMessageTask {
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;
    @TableField("event_key")
    private String eventKey;
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

    /** 获取任务主键。 */
    @Override public Long getTaskId() { return taskId; }
    /** 设置任务主键。 */
    @Override public void setTaskId(Long taskId) { this.taskId = taskId; }
    /** 获取统一事件键。 */
    @Override public String getEventKey() { return eventKey; }
    /** 设置统一事件键。 */
    @Override public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    /** 获取事件类型。 */
    @Override public String getEventType() { return eventType; }
    /** 设置事件类型。 */
    @Override public void setEventType(String eventType) { this.eventType = eventType; }
    /** 获取业务主键。 */
    @Override public String getBusinessKey() { return businessKey; }
    /** 设置业务主键。 */
    @Override public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
    /** 获取载荷 JSON。 */
    @Override public String getPayloadJson() { return payloadJson; }
    /** 设置载荷 JSON。 */
    @Override public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    /** 获取任务状态。 */
    @Override public String getTaskStatus() { return taskStatus; }
    /** 设置任务状态。 */
    @Override public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    /** 获取重试次数。 */
    @Override public Integer getRetryCount() { return retryCount; }
    /** 设置重试次数。 */
    @Override public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    /** 获取最大重试次数。 */
    @Override public Integer getMaxRetryCount() { return maxRetryCount; }
    /** 设置最大重试次数。 */
    @Override public void setMaxRetryCount(Integer maxRetryCount) { this.maxRetryCount = maxRetryCount; }
    /** 获取下一次补发时间。 */
    @Override public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    /** 设置下一次补发时间。 */
    @Override public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    /** 获取最后发送时间。 */
    @Override public LocalDateTime getLastSentAt() { return lastSentAt; }
    /** 设置最后发送时间。 */
    @Override public void setLastSentAt(LocalDateTime lastSentAt) { this.lastSentAt = lastSentAt; }
    /** 获取最后错误信息。 */
    @Override public String getLastErrorMessage() { return lastErrorMessage; }
    /** 设置最后错误信息。 */
    @Override public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }
}
