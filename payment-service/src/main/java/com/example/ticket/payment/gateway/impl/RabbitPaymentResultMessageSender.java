package com.example.ticket.payment.gateway.impl;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.payment.gateway.PaymentResultMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 支付结果消息发送实现。
 * 用于把支付结果事件投递到消息总线，同时把 MQ 细节隔离在网关层。
 */
@Component
public class RabbitPaymentResultMessageSender implements PaymentResultMessageSender {
    private static final Logger log = LoggerFactory.getLogger(RabbitPaymentResultMessageSender.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造支付结果消息发送器。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public RabbitPaymentResultMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送支付结果事件。
     *
     * @param event 支付结果事件
     */
    @Override
    public void send(PaymentResultEvent event) {
        log.debug(
                "发送支付结果事件到 RabbitMQ，eventId={}, paymentRequestId={}, orderId={}, requestId={}",
                event.getEventId(),
                event.getPaymentRequestId(),
                event.getOrderId(),
                event.getRequestId()
        );
        rabbitTemplate.convertAndSend(
                PaymentEventConstants.PAYMENT_EXCHANGE,
                PaymentEventConstants.PAYMENT_RESULT_ROUTING_KEY,
                event
        );
    }
}
