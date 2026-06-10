package com.example.ticket.order.response;

import java.time.LocalDateTime;

/**
 * 抢票结果感知响应。
 * 用于前台按 reservationId 查询预扣与订单当前事实，避免把异步建单空窗误判为失败。
 */
public class ReservationResultResponse {
    private String reservationId;
    private String resultStatus;
    private String reservationStatus;
    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private Long activityId;
    private Long ticketId;
    private Integer quantity;
    private Integer amountCent;
    private LocalDateTime expireAt;
    private LocalDateTime paidAt;
    private LocalDateTime closedAt;

    /** @return 预扣标识 */
    public String getReservationId() { return reservationId; }
    /** @param reservationId 预扣标识 */
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    /** @return 用户侧结果状态 */
    public String getResultStatus() { return resultStatus; }
    /** @param resultStatus 用户侧结果状态 */
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    /** @return 预扣状态 */
    public String getReservationStatus() { return reservationStatus; }
    /** @param reservationStatus 预扣状态 */
    public void setReservationStatus(String reservationStatus) { this.reservationStatus = reservationStatus; }
    /** @return 订单标识 */
    public Long getOrderId() { return orderId; }
    /** @param orderId 订单标识 */
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    /** @return 订单编号 */
    public String getOrderNo() { return orderNo; }
    /** @param orderNo 订单编号 */
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    /** @return 订单状态 */
    public String getOrderStatus() { return orderStatus; }
    /** @param orderStatus 订单状态 */
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    /** @return 活动标识 */
    public Long getActivityId() { return activityId; }
    /** @param activityId 活动标识 */
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    /** @return 票种标识 */
    public Long getTicketId() { return ticketId; }
    /** @param ticketId 票种标识 */
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    /** @return 数量 */
    public Integer getQuantity() { return quantity; }
    /** @param quantity 数量 */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    /** @return 金额，单位分 */
    public Integer getAmountCent() { return amountCent; }
    /** @param amountCent 金额，单位分 */
    public void setAmountCent(Integer amountCent) { this.amountCent = amountCent; }
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
