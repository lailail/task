package com.example.ticket.job.response;

import java.time.LocalDateTime;

/**
 * 预扣记录响应对象。
 * 用于向后台治理页返回稳定的预扣事实视图，不直接暴露持久化对象。
 */
public class ReservationRecordResponse {
    private String reservationId;
    private String requestId;
    private Long userId;
    private Long orderId;
    private Long activityId;
    private Long ticketId;
    private Integer quantity;
    private String reservationStatus;
    private String source;
    private String reason;
    private LocalDateTime expireAt;
    private LocalDateTime releasedAt;

    /** 获取预扣标识。 */
    public String getReservationId() {
        return reservationId;
    }

    /** 设置预扣标识。 */
    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    /** 获取请求标识。 */
    public String getRequestId() {
        return requestId;
    }

    /** 设置请求标识。 */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /** 获取用户标识。 */
    public Long getUserId() {
        return userId;
    }

    /** 设置用户标识。 */
    public void setUserId(Long userId) {
        this.userId = userId;
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

    /** 获取预扣数量。 */
    public Integer getQuantity() {
        return quantity;
    }

    /** 设置预扣数量。 */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /** 获取预扣状态。 */
    public String getReservationStatus() {
        return reservationStatus;
    }

    /** 设置预扣状态。 */
    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    /** 获取来源服务。 */
    public String getSource() {
        return source;
    }

    /** 设置来源服务。 */
    public void setSource(String source) {
        this.source = source;
    }

    /** 获取释放或失败原因。 */
    public String getReason() {
        return reason;
    }

    /** 设置释放或失败原因。 */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /** 获取预扣过期时间。 */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /** 设置预扣过期时间。 */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    /** 获取释放时间。 */
    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    /** 设置释放时间。 */
    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }
}
