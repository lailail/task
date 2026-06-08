package com.example.ticket.payment.request;

/**
 * 支付对账异常查询请求。
 * 用于承接内部查询接口的过滤条件，避免控制层直接拼接查询规则。
 */
public class PaymentReconcileIssueQueryRequest {
    private String paymentRequestId;
    private Long orderId;
    private String issueStatus;

    /** 获取支付请求标识。 */
    public String getPaymentRequestId() { return paymentRequestId; }
    /** 设置支付请求标识。 */
    public void setPaymentRequestId(String paymentRequestId) { this.paymentRequestId = paymentRequestId; }
    /** 获取订单标识。 */
    public Long getOrderId() { return orderId; }
    /** 设置订单标识。 */
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    /** 获取异常状态。 */
    public String getIssueStatus() { return issueStatus; }
    /** 设置异常状态。 */
    public void setIssueStatus(String issueStatus) { this.issueStatus = issueStatus; }
}
