package com.example.ticket.payment.service;

import com.example.ticket.payment.request.PaymentNotifyRequest;

import java.time.LocalDateTime;

/**
 * 支付处理服务接口。
 * 用于收敛支付事实落库、支付结果发布与最小对账回查逻辑。
 */
public interface PaymentProcessService {

    /**
     * 记录支付结果并发布事件。
     *
     * @param request 支付通知请求
     */
    void recordPaymentResult(PaymentNotifyRequest request);

    /**
     * 对待收敛支付记录执行对账回查。
     *
     * @param now 当前时间
     */
    void reconcilePendingPayments(LocalDateTime now);

    /**
     * 对指定支付请求执行一次定向对账回查。
     *
     * @param paymentRequestId 支付请求标识
     * @param now 当前时间
     */
    void reconcilePaymentRequest(String paymentRequestId, LocalDateTime now);
}
