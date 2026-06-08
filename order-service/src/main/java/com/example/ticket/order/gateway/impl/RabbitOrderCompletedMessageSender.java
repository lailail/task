package com.example.ticket.order.gateway.impl;

import com.example.ticket.common.event.order.OrderCompletedEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.order.gateway.OrderCompletedMessageSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 订单完成消息发送实现。
 * 用于把订单完成事件投递到订单域交换机，供后续下游订阅。
 */
@Component
public class RabbitOrderCompletedMessageSender implements OrderCompletedMessageSender {
    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造订单完成消息发送器。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public RabbitOrderCompletedMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送订单完成事件。
     *
     * @param event 订单完成事件
     */
    @Override
    public void send(OrderCompletedEvent event) {
        rabbitTemplate.convertAndSend(
                OrderEventConstants.ORDER_CREATE_EXCHANGE,
                OrderEventConstants.ORDER_COMPLETED_ROUTING_KEY,
                event
        );
    }
}
