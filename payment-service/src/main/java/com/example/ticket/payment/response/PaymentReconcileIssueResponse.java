package com.example.ticket.payment.response;

import java.time.LocalDateTime;

/**
 * 支付对账异常响应对象。
 * 用于向内部查询调用方返回稳定的异常事实视图。
 */
public class PaymentReconcileIssueResponse {
    private Long issueId;
    private String paymentRequestId;
    private Long orderId;
    private String orderNo;
    private String issueType;
    private String issueStatus;
    private String paymentStatus;
    private String orderStatus;
    private String latestErrorMessage;
    private LocalDateTime firstDetectedAt;
    private LocalDateTime lastDetectedAt;
    private LocalDateTime resolvedAt;

    /** 获取异常主键。 */
    public Long getIssueId() { return issueId; }
    /** 设置异常主键。 */
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    /** 获取支付请求标识。 */
    public String getPaymentRequestId() { return paymentRequestId; }
    /** 设置支付请求标识。 */
    public void setPaymentRequestId(String paymentRequestId) { this.paymentRequestId = paymentRequestId; }
    /** 获取订单标识。 */
    public Long getOrderId() { return orderId; }
    /** 设置订单标识。 */
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    /** 获取订单编号。 */
    public String getOrderNo() { return orderNo; }
    /** 设置订单编号。 */
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    /** 获取异常类型。 */
    public String getIssueType() { return issueType; }
    /** 设置异常类型。 */
    public void setIssueType(String issueType) { this.issueType = issueType; }
    /** 获取异常状态。 */
    public String getIssueStatus() { return issueStatus; }
    /** 设置异常状态。 */
    public void setIssueStatus(String issueStatus) { this.issueStatus = issueStatus; }
    /** 获取支付状态。 */
    public String getPaymentStatus() { return paymentStatus; }
    /** 设置支付状态。 */
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    /** 获取订单状态。 */
    public String getOrderStatus() { return orderStatus; }
    /** 设置订单状态。 */
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    /** 获取最近错误信息。 */
    public String getLatestErrorMessage() { return latestErrorMessage; }
    /** 设置最近错误信息。 */
    public void setLatestErrorMessage(String latestErrorMessage) { this.latestErrorMessage = latestErrorMessage; }
    /** 获取首次发现时间。 */
    public LocalDateTime getFirstDetectedAt() { return firstDetectedAt; }
    /** 设置首次发现时间。 */
    public void setFirstDetectedAt(LocalDateTime firstDetectedAt) { this.firstDetectedAt = firstDetectedAt; }
    /** 获取最近发现时间。 */
    public LocalDateTime getLastDetectedAt() { return lastDetectedAt; }
    /** 设置最近发现时间。 */
    public void setLastDetectedAt(LocalDateTime lastDetectedAt) { this.lastDetectedAt = lastDetectedAt; }
    /** 获取解决时间。 */
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    /** 设置解决时间。 */
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
