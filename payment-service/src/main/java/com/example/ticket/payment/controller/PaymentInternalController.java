package com.example.ticket.payment.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.payment.request.PaymentReconcileIssueQueryRequest;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;
import com.example.ticket.payment.service.PaymentReconcileIssueQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支付内部查询控制器。
 * 用于暴露支付对账异常的内部查询接口，不承载人工处置逻辑。
 */
@RestController
@RequestMapping("/api/v1/internal/payment-reconcile")
public class PaymentInternalController {
    private final PaymentReconcileIssueQueryService paymentReconcileIssueQueryService;

    /**
     * 构造支付内部查询控制器。
     *
     * @param paymentReconcileIssueQueryService 支付对账异常查询服务
     */
    public PaymentInternalController(PaymentReconcileIssueQueryService paymentReconcileIssueQueryService) {
        this.paymentReconcileIssueQueryService = paymentReconcileIssueQueryService;
    }

    /**
     * 查询支付对账异常列表。
     *
     * @param request 查询条件
     * @return 异常列表
     */
    @GetMapping("/issues")
    public ApiResponse<List<PaymentReconcileIssueResponse>> queryIssues(PaymentReconcileIssueQueryRequest request) {
        return ApiResponse.success(paymentReconcileIssueQueryService.queryIssues(request));
    }

    /**
     * 查询单条支付对账异常详情。
     *
     * @param issueId 异常标识
     * @return 异常详情
     */
    @GetMapping("/issues/{issueId}")
    public ApiResponse<PaymentReconcileIssueResponse> queryIssueDetail(@PathVariable Long issueId) {
        return ApiResponse.success(paymentReconcileIssueQueryService.queryIssueDetail(issueId));
    }
}
