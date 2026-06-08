package com.example.ticket.payment.gateway;

import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.common.reliable.ReliableMessageSender;

/**
 * 支付收敛消息发送器。
 * 用于屏蔽具体 MQ 客户端实现，让可靠消息模板只依赖统一接口。
 */
public interface PaymentReconciledMessageSender extends ReliableMessageSender<PaymentReconciledEvent> {

    /**
     * 发送支付收敛事件。
     *
     * @param event 支付收敛事件
     */
    @Override
    void send(PaymentReconciledEvent event);
}
