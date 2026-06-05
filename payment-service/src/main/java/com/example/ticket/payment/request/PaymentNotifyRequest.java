package com.example.ticket.payment.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 支付结果通知请求。
 * 用于承接模拟支付域输入的支付结果事实。
 */
public class PaymentNotifyRequest {
    @NotBlank(message = "requestId 不能为空")
    private String requestId;
    @NotBlank(message = "paymentRequestId 不能为空")
    private String paymentRequestId;
    @NotNull(message = "orderId 不能为空")
    private Long orderId;
    @NotBlank(message = "orderNo 不能为空")
    private String orderNo;
    @NotBlank(message = "reservationId 不能为空")
    private String reservationId;
    @NotNull(message = "userId 不能为空")
    private Long userId;
    @NotNull(message = "activityId 不能为空")
    private Long activityId;
    @NotNull(message = "ticketId 不能为空")
    private Long ticketId;
    @NotNull(message = "quantity 不能为空")
    @Min(value = 1, message = "quantity 必须大于 0")
    private Integer quantity;
    @NotBlank(message = "paymentStatus 不能为空")
    private String paymentStatus;
    @NotBlank(message = "reason 不能为空")
    private String reason;

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
     * 获取数量。
     *
     * @return 数量
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * 设置数量。
     *
     * @param quantity 数量
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 获取支付结果类型。
     *
     * @return 支付结果类型
     */
    public String getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * 设置支付结果类型。
     *
     * @param paymentStatus 支付结果类型
     */
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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
}
