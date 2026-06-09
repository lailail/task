package com.example.ticket.payment.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.payment.request.PaymentNotifyRequest;
import com.example.ticket.payment.service.PaymentProcessService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口控制器。
 * 用于接收模拟支付通知请求，并把支付结果处理委托给服务层。
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentProcessService paymentProcessService;

    /**
     * 构造支付控制器。
     *
     * @param paymentProcessService 支付处理服务
     */
    public PaymentController(PaymentProcessService paymentProcessService) {
        this.paymentProcessService = paymentProcessService;
    }

    /**
     * 接收模拟支付结果通知。
     *
     * @param request 支付通知请求
     * @return 通用成功响应
     */
    @PostMapping("/notify")
    public ApiResponse<Void> notifyPayment(@Valid @RequestBody PaymentNotifyRequest request) {
        log.info(
                "收到支付通知请求，paymentRequestId={}, orderId={}, requestId={}, userId={}, activityId={}, ticketId={}, paymentStatus={}",
                request.getPaymentRequestId(),
                request.getOrderId(),
                request.getRequestId(),
                request.getUserId(),
                request.getActivityId(),
                request.getTicketId(),
                request.getPaymentStatus()
        );
        paymentProcessService.recordPaymentResult(request);
        log.info(
                "支付通知处理完成，paymentRequestId={}, orderId={}, requestId={}",
                request.getPaymentRequestId(),
                request.getOrderId(),
                request.getRequestId()
        );
        return ApiResponse.success(null);
    }
}
