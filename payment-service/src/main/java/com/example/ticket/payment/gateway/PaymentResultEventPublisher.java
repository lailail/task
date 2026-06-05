package com.example.ticket.payment.gateway;

import com.example.ticket.common.event.payment.PaymentResultEvent;

/**
 * 支付结果事件发布器。
 * 用于把支付事实异步发送给订单域。
 */
public interface PaymentResultEventPublisher {

    /**
     * 发布支付结果事件。
     *
     * @param event 支付结果事件
     */
    void publish(PaymentResultEvent event);
}
