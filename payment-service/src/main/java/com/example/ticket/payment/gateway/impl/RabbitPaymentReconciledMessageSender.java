package com.example.ticket.payment.gateway.impl;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.payment.gateway.PaymentReconciledMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 支付收敛消息发送实现。
 * 用于把支付收敛事件投递到支付域交换机。
 */
@Component
public class RabbitPaymentReconciledMessageSender implements PaymentReconciledMessageSender {
    private static final Logger log = LoggerFactory.getLogger(RabbitPaymentReconciledMessageSender.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造支付收敛消息发送器。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public RabbitPaymentReconciledMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送支付收敛事件。
     *
     * @param event 支付收敛事件
     */
    @Override
    public void send(PaymentReconciledEvent event) {
        log.debug(
                "发送支付收敛事件到 RabbitMQ，eventId={}, paymentRequestId={}, orderId={}, requestId={}",
                event.getEventId(),
                event.getPaymentRequestId(),
                event.getOrderId(),
                event.getRequestId()
        );
        rabbitTemplate.convertAndSend(
                PaymentEventConstants.PAYMENT_EXCHANGE,
                PaymentEventConstants.PAYMENT_RECONCILED_ROUTING_KEY,
                event
        );
    }
}
