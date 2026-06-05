package com.example.ticket.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 订单事件日志持久化对象。
 * 用于映射 `order_event_log` 表，为后续幂等消费和失败重试提供持久化基础。
 */
@TableName("order_event_log")
public class OrderEventLogDO {
    @TableId(value = "event_id", type = IdType.AUTO)
    private Long eventId;
    @TableField("event_key")
    private String eventKey;
    @TableField("event_type")
    private String eventType;
    @TableField("business_key")
    private String businessKey;
    @TableField("payload_json")
    private String payloadJson;
    @TableField("consume_status")
    private String consumeStatus;
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 获取事件主键。
     *
     * @return 事件主键
     */
    public Long getEventId() {
        return eventId;
    }

    /**
     * 设置事件主键。
     *
     * @param eventId 事件主键
     */
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * 获取事件幂等键。
     *
     * @return 事件幂等键
     */
    public String getEventKey() {
        return eventKey;
    }

    /**
     * 设置事件幂等键。
     *
     * @param eventKey 事件幂等键
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
     * 获取事件载荷。
     *
     * @return 事件载荷
     */
    public String getPayloadJson() {
        return payloadJson;
    }

    /**
     * 设置事件载荷。
     *
     * @param payloadJson 事件载荷
     */
    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    /**
     * 获取消费状态。
     *
     * @return 消费状态
     */
    public String getConsumeStatus() {
        return consumeStatus;
    }

    /**
     * 设置消费状态。
     *
     * @param consumeStatus 消费状态
     */
    public void setConsumeStatus(String consumeStatus) {
        this.consumeStatus = consumeStatus;
    }

    /**
     * 获取重试次数。
     *
     * @return 重试次数
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 设置重试次数。
     *
     * @param retryCount 重试次数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}
