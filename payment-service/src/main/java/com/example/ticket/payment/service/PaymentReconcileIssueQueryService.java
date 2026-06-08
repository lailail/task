package com.example.ticket.payment.service;

import com.example.ticket.payment.request.PaymentReconcileIssueQueryRequest;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;

import java.util.List;

/**
 * 支付对账异常查询服务。
 * 用于向内部调用方暴露稳定的异常事实查询能力。
 */
public interface PaymentReconcileIssueQueryService {

    /**
     * 查询异常列表。
     *
     * @param request 查询条件
     * @return 异常列表
     */
    List<PaymentReconcileIssueResponse> queryIssues(PaymentReconcileIssueQueryRequest request);

    /**
     * 查询异常详情。
     *
     * @param issueId 异常标识
     * @return 异常详情
     */
    PaymentReconcileIssueResponse queryIssueDetail(Long issueId);
}
