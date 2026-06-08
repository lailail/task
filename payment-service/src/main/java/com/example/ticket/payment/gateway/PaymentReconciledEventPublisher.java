package com.example.ticket.payment.gateway;

import com.example.ticket.common.event.payment.PaymentReconciledEvent;

/**
 * 支付收敛事件发布器。
 * 用于在支付事实与订单状态确认收敛后发布正式后置事件。
 */
public interface PaymentReconciledEventPublisher {

    /**
     * 发布支付收敛事件。
     *
     * @param event 支付收敛事件
     */
    void publish(PaymentReconciledEvent event);
}
