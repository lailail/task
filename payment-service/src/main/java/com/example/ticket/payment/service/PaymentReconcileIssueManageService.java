package com.example.ticket.payment.service;

import com.example.ticket.payment.request.PaymentReconcileIssueHandleRequest;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;

/**
 * 支付对账异常人工治理服务接口。
 * 用于对外暴露人工重试、人工忽略和人工解决等治理动作，避免控制层直接改动异常事实与支付状态。
 */
public interface PaymentReconcileIssueManageService {

    /**
     * 对指定异常执行人工重试。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    PaymentReconcileIssueResponse retryIssue(Long issueId, PaymentReconcileIssueHandleRequest request);

    /**
     * 对指定异常执行人工忽略。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    PaymentReconcileIssueResponse ignoreIssue(Long issueId, PaymentReconcileIssueHandleRequest request);

    /**
     * 对指定异常执行人工解决。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    PaymentReconcileIssueResponse resolveIssue(Long issueId, PaymentReconcileIssueHandleRequest request);
}
