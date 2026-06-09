package com.example.ticket.order.gateway.impl;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.order.gateway.OrderCreateResultMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 下单结果事件发布实现。
 * 用于把订单创建成功或失败的结果异步通知给后续状态收敛处理方。
 */
@Component
public class RabbitOrderCreateResultEventPublisher implements OrderCreateResultMessageSender {
    private static final Logger log = LoggerFactory.getLogger(RabbitOrderCreateResultEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造结果事件发布器。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public RabbitOrderCreateResultEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布下单结果事件。
     *
     * @param event 下单结果事件
     */
    @Override
    public void send(OrderCreateResultEvent event) {
        log.debug(
                "发送下单结果事件到 RabbitMQ，eventId={}, eventType={}, reservationId={}, orderId={}, requestId={}",
                event.getEventId(),
                event.getEventType(),
                event.getReservationId(),
                event.getOrderId(),
                event.getRequestId()
        );
        rabbitTemplate.convertAndSend(
                OrderEventConstants.ORDER_CREATE_EXCHANGE,
                OrderEventConstants.ORDER_RESULT_ROUTING_KEY,
                event
        );
    }
}
