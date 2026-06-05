package com.example.ticket.seckill.gateway.impl;

import com.example.ticket.seckill.event.OrderCreateRequestedEvent;
import com.example.ticket.seckill.gateway.OrderCreateEventPublisher;
import com.example.ticket.seckill.support.SeckillConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 下单事件发布实现。
 * 当前负责把预扣成功后的下单事件发送到约定路由。
 */
@Component
public class RabbitOrderCreateEventPublisher implements OrderCreateEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造 RabbitMQ 事件发布器。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public RabbitOrderCreateEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布下单事件。
     *
     * @param event 下单事件
     */
    @Override
    public void publish(OrderCreateRequestedEvent event) {
        rabbitTemplate.convertAndSend(
                SeckillConstants.ORDER_CREATE_EXCHANGE,
                SeckillConstants.ORDER_CREATE_ROUTING_KEY,
                event
        );
    }
}
