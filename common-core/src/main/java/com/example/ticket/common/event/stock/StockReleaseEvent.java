package com.example.ticket.common.event.stock;

import java.io.Serializable;
import java.time.Instant;

/**
 * 库存释放事件。
 * 用于在建单失败、订单关闭或补偿回退时，把库存回补所需的最小上下文异步传递给释放处理方。
 */
public class StockReleaseEvent implements Serializable {
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
    private String reason;
    private String source;

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
     * 获取事件发生时间。
     *
     * @return 事件发生时间
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * 设置事件发生时间。
     *
     * @param occurredAt 事件发生时间
     */
    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    /**
     * 获取请求标识。
     *
     * @return 请求标识
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 设置请求标识。
     *
     * @param requestId 请求标识
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取业务幂等键。
     *
     * @return 业务幂等键
     */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * 设置业务幂等键。
     *
     * @param idempotencyKey 业务幂等键
     */
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    /**
     * 获取预扣标识。
     *
     * @return 预扣标识
     */
    public String getReservationId() {
        return reservationId;
    }

    /**
     * 设置预扣标识。
     *
     * @param reservationId 预扣标识
     */
    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * 获取订单标识。
     *
     * @return 订单标识
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * 设置订单标识。
     *
     * @param orderId 订单标识
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * 获取活动标识。
     *
     * @return 活动标识
     */
    public Long getActivityId() {
        return activityId;
    }

    /**
     * 设置活动标识。
     *
     * @param activityId 活动标识
     */
    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    /**
     * 获取票种标识。
     *
     * @return 票种标识
     */
    public Long getTicketId() {
        return ticketId;
    }

    /**
     * 设置票种标识。
     *
     * @param ticketId 票种标识
     */
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * 获取用户标识。
     *
     * @return 用户标识
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户标识。
     *
     * @param userId 用户标识
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取回补数量。
     *
     * @return 回补数量
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * 设置回补数量。
     *
     * @param quantity 回补数量
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 获取释放原因。
     *
     * @return 释放原因
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置释放原因。
     *
     * @param reason 释放原因
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取来源服务。
     *
     * @return 来源服务
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置来源服务。
     *
     * @param source 来源服务
     */
    public void setSource(String source) {
        this.source = source;
    }
}
