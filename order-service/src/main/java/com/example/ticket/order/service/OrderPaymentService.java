package com.example.ticket.order.service;

import com.example.ticket.common.event.payment.PaymentResultEvent;

/**
 * 订单支付结果处理服务接口。
 * 用于把支付域回推的结果事件收敛为订单状态流转，避免消费者直接承载业务细节。
 */
public interface OrderPaymentService {

    /**
     * 处理支付结果事件。
     *
     * @param event 支付结果事件
     */
    void handlePaymentResult(PaymentResultEvent event);
}
