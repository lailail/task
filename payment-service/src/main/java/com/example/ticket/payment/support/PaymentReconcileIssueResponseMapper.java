package com.example.ticket.payment.support;

import com.example.ticket.payment.domain.PaymentReconcileIssueDO;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;

/**
 * 支付对账异常响应映射工具。
 * 用于把异常事实统一转换为对外响应视图，避免查询服务和治理服务重复拼装字段。
 */
public final class PaymentReconcileIssueResponseMapper {

    /**
     * 禁止实例化映射工具类。
     */
    private PaymentReconcileIssueResponseMapper() {
    }

    /**
     * 把异常持久化对象转换为响应对象。
     *
     * @param issue 异常持久化对象
     * @return 异常响应对象
     */
    public static PaymentReconcileIssueResponse toResponse(PaymentReconcileIssueDO issue) {
        PaymentReconcileIssueResponse response = new PaymentReconcileIssueResponse();
        response.setIssueId(issue.getIssueId());
        response.setPaymentRequestId(issue.getPaymentRequestId());
        response.setOrderId(issue.getOrderId());
        response.setOrderNo(issue.getOrderNo());
        response.setIssueType(issue.getIssueType());
        response.setIssueStatus(issue.getIssueStatus());
        response.setPaymentStatus(issue.getPaymentStatus());
        response.setOrderStatus(issue.getOrderStatus());
        response.setLatestErrorMessage(issue.getLatestErrorMessage());
        response.setFirstDetectedAt(issue.getFirstDetectedAt());
        response.setLastDetectedAt(issue.getLastDetectedAt());
        response.setResolvedAt(issue.getResolvedAt());
        response.setManualAction(issue.getManualAction());
        response.setManualOperator(issue.getManualOperator());
        response.setManualNote(issue.getManualNote());
        response.setManualOperatedAt(issue.getManualOperatedAt());
        return response;
    }
}
