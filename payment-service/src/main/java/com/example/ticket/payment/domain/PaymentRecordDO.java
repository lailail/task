package com.example.ticket.payment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 支付记录持久化对象。
 * 用于映射 `payment_record` 表，沉淀支付事实与对账状态。
 */
@TableName("payment_record")
public class PaymentRecordDO {
    @TableId(value = "payment_id", type = IdType.AUTO)
    private Long paymentId;
    @TableField("payment_request_id")
    private String paymentRequestId;
    @TableField("order_id")
    private Long orderId;
    @TableField("order_no")
    private String orderNo;
    @TableField("reservation_id")
    private String reservationId;
    @TableField("request_id")
    private String requestId;
    @TableField("user_id")
    private Long userId;
    @TableField("activity_id")
    private Long activityId;
    @TableField("ticket_id")
    private Long ticketId;
    private Integer quantity;
    @TableField("payment_status")
    private String paymentStatus;
    @TableField("reconcile_status")
    private String reconcileStatus;
    private String reason;
    @TableField("paid_at")
    private LocalDateTime paidAt;
    @TableField("last_reconcile_at")
    private LocalDateTime lastReconcileAt;

    /**
     * 获取支付主键。
     *
     * @return 支付主键
     */
    public Long getPaymentId() {
        return paymentId;
    }

    /**
     * 设置支付主键。
     *
     * @param paymentId 支付主键
     */
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    /**
     * 获取支付请求标识。
     *
     * @return 支付请求标识
     */
    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    /**
     * 设置支付请求标识。
     *
     * @param paymentRequestId 支付请求标识
     */
    public void setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
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
     * 获取订单编号。
     *
     * @return 订单编号
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * 设置订单编号。
     *
     * @param orderNo 订单编号
     */
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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
     * 获取购票数量。
     *
     * @return 购票数量
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * 设置购票数量。
     *
     * @param quantity 购票数量
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 获取支付状态。
     *
     * @return 支付状态
     */
    public String getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * 设置支付状态。
     *
     * @param paymentStatus 支付状态
     */
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    /**
     * 获取对账状态。
     *
     * @return 对账状态
     */
    public String getReconcileStatus() {
        return reconcileStatus;
    }

    /**
     * 设置对账状态。
     *
     * @param reconcileStatus 对账状态
     */
    public void setReconcileStatus(String reconcileStatus) {
        this.reconcileStatus = reconcileStatus;
    }

    /**
     * 获取结果原因。
     *
     * @return 结果原因
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置结果原因。
     *
     * @param reason 结果原因
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取支付完成时间。
     *
     * @return 支付完成时间
     */
    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    /**
     * 设置支付完成时间。
     *
     * @param paidAt 支付完成时间
     */
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    /**
     * 获取最近一次对账时间。
     *
     * @return 最近一次对账时间
     */
    public LocalDateTime getLastReconcileAt() {
        return lastReconcileAt;
    }

    /**
     * 设置最近一次对账时间。
     *
     * @param lastReconcileAt 最近一次对账时间
     */
    public void setLastReconcileAt(LocalDateTime lastReconcileAt) {
        this.lastReconcileAt = lastReconcileAt;
    }
}
