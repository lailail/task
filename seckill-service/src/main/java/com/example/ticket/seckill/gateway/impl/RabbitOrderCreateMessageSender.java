package com.example.ticket.seckill.gateway.impl;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.seckill.gateway.OrderCreateMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 下单请求消息发送器。
 * 用于把抢票成功后的下单请求事件发送到约定路由。
 */
@Component
public class RabbitOrderCreateMessageSender implements OrderCreateMessageSender {
    private static final Logger log = LoggerFactory.getLogger(RabbitOrderCreateMessageSender.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造 RabbitMQ 下单请求消息发送器。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public RabbitOrderCreateMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送下单请求事件。
     *
     * @param event 下单请求事件
     */
    @Override
    public void send(OrderCreateRequestedEvent event) {
        log.debug(
                "发送建单请求事件到 RabbitMQ，eventId={}, reservationId={}, userId={}, requestId={}, idempotencyKey={}",
                event.getEventId(),
                event.getReservationId(),
                event.getUserId(),
                event.getRequestId(),
                event.getIdempotencyKey()
        );
        rabbitTemplate.convertAndSend(
                OrderEventConstants.ORDER_CREATE_EXCHANGE,
                OrderEventConstants.ORDER_CREATE_ROUTING_KEY,
                event
        );
    }
}
