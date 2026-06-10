package com.example.ticket.order.response;

import java.time.LocalDateTime;

/**
 * 用户订单响应。
 * 用于前台“我的订单”页面展示最小订单事实，不暴露订单持久化对象。
 */
public class UserOrderResponse {
    private Long orderId;
    private String orderNo;
    private String reservationId;
    private Long activityId;
    private Long ticketId;
    private Integer quantity;
    private Integer amountCent;
    private String orderStatus;
    private LocalDateTime expireAt;
    private LocalDateTime paidAt;
    private LocalDateTime closedAt;

    /** @return 订单标识 */
    public Long getOrderId() { return orderId; }
    /** @param orderId 订单标识 */
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    /** @return 订单编号 */
    public String getOrderNo() { return orderNo; }
    /** @param orderNo 订单编号 */
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    /** @return 预扣标识 */
    public String getReservationId() { return reservationId; }
    /** @param reservationId 预扣标识 */
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    /** @return 活动标识 */
    public Long getActivityId() { return activityId; }
    /** @param activityId 活动标识 */
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    /** @return 票种标识 */
    public Long getTicketId() { return ticketId; }
    /** @param ticketId 票种标识 */
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    /** @return 购票数量 */
    public Integer getQuantity() { return quantity; }
    /** @param quantity 购票数量 */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    /** @return 订单金额，单位分 */
    public Integer getAmountCent() { return amountCent; }
    /** @param amountCent 订单金额，单位分 */
    public void setAmountCent(Integer amountCent) { this.amountCent = amountCent; }
    /** @return 订单状态 */
    public String getOrderStatus() { return orderStatus; }
    /** @param orderStatus 订单状态 */
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    /** @return 过期时间 */
    public LocalDateTime getExpireAt() { return expireAt; }
    /** @param expireAt 过期时间 */
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    /** @return 支付时间 */
    public LocalDateTime getPaidAt() { return paidAt; }
    /** @param paidAt 支付时间 */
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    /** @return 关闭时间 */
    public LocalDateTime getClosedAt() { return closedAt; }
    /** @param closedAt 关闭时间 */
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
