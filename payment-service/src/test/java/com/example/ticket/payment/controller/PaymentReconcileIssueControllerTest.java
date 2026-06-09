package com.example.ticket.payment.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.payment.request.PaymentReconcileIssueHandleRequest;
import com.example.ticket.payment.request.PaymentReconcileIssueQueryRequest;
import com.example.ticket.payment.response.PaymentReconcileIssueResponse;
import com.example.ticket.payment.service.PaymentReconcileIssueManageService;
import com.example.ticket.payment.service.PaymentReconcileIssueQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付对账异常内部查询控制器单元测试。
 * 用于固定控制层只负责委托查询服务和封装响应，不承载业务判断。
 */
@ExtendWith(MockitoExtension.class)
class PaymentReconcileIssueControllerTest {

    @Mock
    private PaymentReconcileIssueQueryService paymentReconcileIssueQueryService;

    @Mock
    private PaymentReconcileIssueManageService paymentReconcileIssueManageService;

    /**
     * 查询异常列表时，应委托给查询服务并返回统一响应体。
     */
    @Test
    void should_delegate_issue_list_query_to_service() {
        PaymentInternalController controller = new PaymentInternalController(
                paymentReconcileIssueQueryService,
                paymentReconcileIssueManageService
        );
        PaymentReconcileIssueQueryRequest request = new PaymentReconcileIssueQueryRequest();
        request.setPaymentRequestId("payment-request-001");
        when(paymentReconcileIssueQueryService.queryIssues(request)).thenReturn(List.of(buildIssueResponse()));

        ApiResponse<List<PaymentReconcileIssueResponse>> response = controller.queryIssues(request);

        verify(paymentReconcileIssueQueryService).queryIssues(request);
        assertEquals(1, response.getData().size());
        assertEquals("payment-request-001", response.getData().get(0).getPaymentRequestId());
    }

    /**
     * 查询异常详情时，应委托给查询服务并返回单条结果。
     */
    @Test
    void should_delegate_issue_detail_query_to_service() {
        PaymentInternalController controller = new PaymentInternalController(
                paymentReconcileIssueQueryService,
                paymentReconcileIssueManageService
        );
        when(paymentReconcileIssueQueryService.queryIssueDetail(11L)).thenReturn(buildIssueResponse());

        ApiResponse<PaymentReconcileIssueResponse> response = controller.queryIssueDetail(11L);

        verify(paymentReconcileIssueQueryService).queryIssueDetail(11L);
        assertEquals(11L, response.getData().getIssueId());
    }

    /**
     * 人工重试时，应委托给治理服务并返回更新后的异常详情。
     */
    @Test
    void should_delegate_issue_retry_to_manage_service() {
        PaymentInternalController controller = new PaymentInternalController(
                paymentReconcileIssueQueryService,
                paymentReconcileIssueManageService
        );
        PaymentReconcileIssueHandleRequest request = buildHandleRequest();
        when(paymentReconcileIssueManageService.retryIssue(11L, request)).thenReturn(buildIssueResponse());

        ApiResponse<PaymentReconcileIssueResponse> response = controller.retryIssue(11L, request);

        verify(paymentReconcileIssueManageService).retryIssue(11L, request);
        assertEquals(11L, response.getData().getIssueId());
    }

    /**
     * 人工忽略时，应委托给治理服务并返回更新后的异常详情。
     */
    @Test
    void should_delegate_issue_ignore_to_manage_service() {
        PaymentInternalController controller = new PaymentInternalController(
                paymentReconcileIssueQueryService,
                paymentReconcileIssueManageService
        );
        PaymentReconcileIssueHandleRequest request = buildHandleRequest();
        when(paymentReconcileIssueManageService.ignoreIssue(11L, request)).thenReturn(buildIssueResponse());

        ApiResponse<PaymentReconcileIssueResponse> response = controller.ignoreIssue(11L, request);

        verify(paymentReconcileIssueManageService).ignoreIssue(11L, request);
        assertEquals(11L, response.getData().getIssueId());
    }

    /**
     * 人工解决时，应委托给治理服务并返回更新后的异常详情。
     */
    @Test
    void should_delegate_issue_resolve_to_manage_service() {
        PaymentInternalController controller = new PaymentInternalController(
                paymentReconcileIssueQueryService,
                paymentReconcileIssueManageService
        );
        PaymentReconcileIssueHandleRequest request = buildHandleRequest();
        when(paymentReconcileIssueManageService.resolveIssue(11L, request)).thenReturn(buildIssueResponse());

        ApiResponse<PaymentReconcileIssueResponse> response = controller.resolveIssue(11L, request);

        verify(paymentReconcileIssueManageService).resolveIssue(11L, request);
        assertEquals(11L, response.getData().getIssueId());
    }

    /**
     * 构造测试用异常响应。
     *
     * @return 异常响应
     */
    private PaymentReconcileIssueResponse buildIssueResponse() {
        PaymentReconcileIssueResponse response = new PaymentReconcileIssueResponse();
        response.setIssueId(11L);
        response.setPaymentRequestId("payment-request-001");
        response.setOrderId(20001L);
        response.setOrderNo("ORD-001");
        return response;
    }

    /**
     * 构造人工处置请求。
     *
     * @return 人工处置请求
     */
    private PaymentReconcileIssueHandleRequest buildHandleRequest() {
        PaymentReconcileIssueHandleRequest request = new PaymentReconcileIssueHandleRequest();
        request.setOperator("tester");
        request.setNote("manual");
        return request;
    }
}
