package com.example.ticket.common.event.order;

import java.io.Serializable;
import java.time.Instant;

/**
 * 下单结果事件。
 * 用于把 `order-service` 的建单结果异步通知给后续状态收敛和补偿处理方。
 */
public class OrderCreateResultEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private String requestId;
    private String idempotencyKey;
    private String reservationId;
    private Long orderId;
    private Long activityId;
    private Long ticketId;
    private Long userId;
    private Integer quantity;
    private String status;
    private String reason;
    private String source;

    /** 获取事件标识。 */
    public String getEventId() {
        return eventId;
    }

    /** 设置事件标识。 */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /** 获取事件类型。 */
    public String getEventType() {
        return eventType;
    }

    /** 设置事件类型。 */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /** 获取事件发生时间。 */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /** 设置事件发生时间。 */
    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    /** 获取请求标识。 */
    public String getRequestId() {
        return requestId;
    }

    /** 设置请求标识。 */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /** 获取业务幂等键。 */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /** 设置业务幂等键。 */
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    /** 获取预扣标识。 */
    public String getReservationId() {
        return reservationId;
    }

    /** 设置预扣标识。 */
    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    /** 获取订单标识。 */
    public Long getOrderId() {
        return orderId;
    }

    /** 设置订单标识。 */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /** 获取活动标识。 */
    public Long getActivityId() {
        return activityId;
    }

    /** 设置活动标识。 */
    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    /** 获取票种标识。 */
    public Long getTicketId() {
        return ticketId;
    }

    /** 设置票种标识。 */
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    /** 获取用户标识。 */
    public Long getUserId() {
        return userId;
    }

    /** 设置用户标识。 */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** 获取购买数量。 */
    public Integer getQuantity() {
        return quantity;
    }

    /** 设置购买数量。 */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /** 获取订单状态。 */
    public String getStatus() {
        return status;
    }

    /** 设置订单状态。 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 获取失败原因。 */
    public String getReason() {
        return reason;
    }

    /** 设置失败原因。 */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /** 获取来源服务。 */
    public String getSource() {
        return source;
    }

    /** 设置来源服务。 */
    public void setSource(String source) {
        this.source = source;
    }
}
