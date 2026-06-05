package com.example.ticket.common.event.order;

import java.io.Serializable;
import java.time.Instant;

/**
 * 下单请求事件。
 * 用于在抢票预扣成功后把建单请求从 `seckill-service` 传递给 `order-service`。
 */
public class OrderCreateRequestedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private String requestId;
    private String idempotencyKey;
    private String reservationId;
    private Long activityId;
    private Long ticketId;
    private Long userId;
    private Integer quantity;
    private Instant expireAt;
    private String source;
    private Long orderId;
    private String traceId;

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

    /** 获取预扣过期时间。 */
    public Instant getExpireAt() {
        return expireAt;
    }

    /** 设置预扣过期时间。 */
    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }

    /** 获取来源服务。 */
    public String getSource() {
        return source;
    }

    /** 设置来源服务。 */
    public void setSource(String source) {
        this.source = source;
    }

    /** 获取订单标识。 */
    public Long getOrderId() {
        return orderId;
    }

    /** 设置订单标识。 */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /** 获取链路追踪标识。 */
    public String getTraceId() {
        return traceId;
    }

    /** 设置链路追踪标识。 */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
