package com.example.ticket.payment.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.payment.request.PaymentReconcileIssueHandleRequest;
import com.example.ticket.payment.request.PaymentReconcileIssueQueryRequest;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;
import com.example.ticket.payment.service.PaymentReconcileIssueManageService;
import com.example.ticket.payment.service.PaymentReconcileIssueQueryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private static final Logger log = LoggerFactory.getLogger(PaymentInternalController.class);

    private final PaymentReconcileIssueQueryService paymentReconcileIssueQueryService;
    private final PaymentReconcileIssueManageService paymentReconcileIssueManageService;

    /**
     * 构造支付内部查询控制器。
     *
     * @param paymentReconcileIssueQueryService 支付对账异常查询服务
     * @param paymentReconcileIssueManageService 支付对账异常人工治理服务
     */
    public PaymentInternalController(
            PaymentReconcileIssueQueryService paymentReconcileIssueQueryService,
            PaymentReconcileIssueManageService paymentReconcileIssueManageService
    ) {
        this.paymentReconcileIssueQueryService = paymentReconcileIssueQueryService;
        this.paymentReconcileIssueManageService = paymentReconcileIssueManageService;
    }

    /**
     * 查询支付对账异常列表。
     *
     * @param request 查询条件
     * @return 异常列表
     */
    @GetMapping("/issues")
    public ApiResponse<List<PaymentReconcileIssueResponse>> queryIssues(PaymentReconcileIssueQueryRequest request) {
        log.info(
                "收到支付对账异常列表查询请求，paymentRequestId={}, orderId={}, issueStatus={}",
                request.getPaymentRequestId(),
                request.getOrderId(),
                request.getIssueStatus()
        );
        List<PaymentReconcileIssueResponse> responses = paymentReconcileIssueQueryService.queryIssues(request);
        log.info("支付对账异常列表查询完成，count={}", responses.size());
        return ApiResponse.success(responses);
    }

    /**
     * 查询单条支付对账异常详情。
     *
     * @param issueId 异常标识
     * @return 异常详情
     */
    @GetMapping("/issues/{issueId}")
    public ApiResponse<PaymentReconcileIssueResponse> queryIssueDetail(@PathVariable Long issueId) {
        log.info("收到支付对账异常详情查询请求，issueId={}", issueId);
        PaymentReconcileIssueResponse response = paymentReconcileIssueQueryService.queryIssueDetail(issueId);
        log.info("支付对账异常详情查询完成，issueId={}, issueStatus={}", issueId, response.getIssueStatus());
        return ApiResponse.success(response);
    }

    /**
     * 对指定异常执行人工重试。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    @PostMapping("/issues/{issueId}/retry")
    public ApiResponse<PaymentReconcileIssueResponse> retryIssue(
            @PathVariable Long issueId,
            @Valid @RequestBody PaymentReconcileIssueHandleRequest request
    ) {
        log.info("收到支付对账异常人工重试请求，issueId={}, operator={}", issueId, request.getOperator());
        PaymentReconcileIssueResponse response = paymentReconcileIssueManageService.retryIssue(issueId, request);
        log.info("支付对账异常人工重试完成，issueId={}, operator={}, issueStatus={}", issueId, request.getOperator(), response.getIssueStatus());
        return ApiResponse.success(response);
    }

    /**
     * 对指定异常执行人工忽略。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    @PostMapping("/issues/{issueId}/ignore")
    public ApiResponse<PaymentReconcileIssueResponse> ignoreIssue(
            @PathVariable Long issueId,
            @Valid @RequestBody PaymentReconcileIssueHandleRequest request
    ) {
        log.info("收到支付对账异常人工忽略请求，issueId={}, operator={}", issueId, request.getOperator());
        PaymentReconcileIssueResponse response = paymentReconcileIssueManageService.ignoreIssue(issueId, request);
        log.info("支付对账异常人工忽略完成，issueId={}, operator={}, issueStatus={}", issueId, request.getOperator(), response.getIssueStatus());
        return ApiResponse.success(response);
    }

    /**
     * 对指定异常执行人工解决。
     *
     * @param issueId 异常标识
     * @param request 处置请求
     * @return 更新后的异常详情
     */
    @PostMapping("/issues/{issueId}/resolve")
    public ApiResponse<PaymentReconcileIssueResponse> resolveIssue(
            @PathVariable Long issueId,
            @Valid @RequestBody PaymentReconcileIssueHandleRequest request
    ) {
        log.info("收到支付对账异常人工解决请求，issueId={}, operator={}", issueId, request.getOperator());
        PaymentReconcileIssueResponse response = paymentReconcileIssueManageService.resolveIssue(issueId, request);
        log.info("支付对账异常人工解决完成，issueId={}, operator={}, issueStatus={}", issueId, request.getOperator(), response.getIssueStatus());
        return ApiResponse.success(response);
    }
}
