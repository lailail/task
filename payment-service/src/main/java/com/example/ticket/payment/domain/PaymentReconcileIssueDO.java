package com.example.ticket.payment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 支付对账异常持久化对象。
 * 用于映射 `payment_reconcile_issue` 表，沉淀支付事实与订单状态未收敛的异常事实。
 */
@TableName("payment_reconcile_issue")
public class PaymentReconcileIssueDO {
    @TableId(value = "issue_id", type = IdType.AUTO)
    private Long issueId;
    @TableField("payment_request_id")
    private String paymentRequestId;
    @TableField("order_id")
    private Long orderId;
    @TableField("order_no")
    private String orderNo;
    @TableField("reservation_id")
    private String reservationId;
    @TableField("user_id")
    private Long userId;
    @TableField("activity_id")
    private Long activityId;
    @TableField("ticket_id")
    private Long ticketId;
    @TableField("quantity")
    private Integer quantity;
    @TableField("payment_status")
    private String paymentStatus;
    @TableField("order_status")
    private String orderStatus;
    @TableField("issue_type")
    private String issueType;
    @TableField("issue_status")
    private String issueStatus;
    @TableField("latest_error_message")
    private String latestErrorMessage;
    @TableField("first_detected_at")
    private LocalDateTime firstDetectedAt;
    @TableField("last_detected_at")
    private LocalDateTime lastDetectedAt;
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;
    @TableField("manual_action")
    private String manualAction;
    @TableField("manual_operator")
    private String manualOperator;
    @TableField("manual_note")
    private String manualNote;
    @TableField("manual_operated_at")
    private LocalDateTime manualOperatedAt;

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
    /** 获取预扣标识。 */
    public String getReservationId() { return reservationId; }
    /** 设置预扣标识。 */
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    /** 获取用户标识。 */
    public Long getUserId() { return userId; }
    /** 设置用户标识。 */
    public void setUserId(Long userId) { this.userId = userId; }
    /** 获取活动标识。 */
    public Long getActivityId() { return activityId; }
    /** 设置活动标识。 */
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    /** 获取票种标识。 */
    public Long getTicketId() { return ticketId; }
    /** 设置票种标识。 */
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    /** 获取数量。 */
    public Integer getQuantity() { return quantity; }
    /** 设置数量。 */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    /** 获取支付状态。 */
    public String getPaymentStatus() { return paymentStatus; }
    /** 设置支付状态。 */
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    /** 获取订单状态。 */
    public String getOrderStatus() { return orderStatus; }
    /** 设置订单状态。 */
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    /** 获取异常类型。 */
    public String getIssueType() { return issueType; }
    /** 设置异常类型。 */
    public void setIssueType(String issueType) { this.issueType = issueType; }
    /** 获取异常状态。 */
    public String getIssueStatus() { return issueStatus; }
    /** 设置异常状态。 */
    public void setIssueStatus(String issueStatus) { this.issueStatus = issueStatus; }
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
    /** 获取最近一次人工处置动作。 */
    public String getManualAction() { return manualAction; }
    /** 设置最近一次人工处置动作。 */
    public void setManualAction(String manualAction) { this.manualAction = manualAction; }
    /** 获取最近一次人工处置操作人。 */
    public String getManualOperator() { return manualOperator; }
    /** 设置最近一次人工处置操作人。 */
    public void setManualOperator(String manualOperator) { this.manualOperator = manualOperator; }
    /** 获取最近一次人工处置备注。 */
    public String getManualNote() { return manualNote; }
    /** 设置最近一次人工处置备注。 */
    public void setManualNote(String manualNote) { this.manualNote = manualNote; }
    /** 获取最近一次人工处置时间。 */
    public LocalDateTime getManualOperatedAt() { return manualOperatedAt; }
    /** 设置最近一次人工处置时间。 */
    public void setManualOperatedAt(LocalDateTime manualOperatedAt) { this.manualOperatedAt = manualOperatedAt; }
}
