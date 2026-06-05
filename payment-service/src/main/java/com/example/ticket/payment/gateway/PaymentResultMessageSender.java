package com.example.ticket.payment.gateway;

import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.common.reliable.ReliableMessageSender;

/**
 * 支付结果消息发送器。
 * 用于屏蔽具体 MQ 客户端实现，让可靠消息模板只依赖统一发送接口。
 */
public interface PaymentResultMessageSender extends ReliableMessageSender<PaymentResultEvent> {

    /**
     * 发送支付结果事件。
     *
     * @param event 支付结果事件
     */
    @Override
    void send(PaymentResultEvent event);
}
