package com.example.ticket.order.consumer;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.order.service.OrderCreateService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 下单请求事件消费者。
 * 用于承接 `ticket.order.create` 消息，并把实际业务处理委托给下单应用服务。
 */
@Component
public class OrderCreateEventConsumer {
    private final OrderCreateService orderCreateService;

    /**
     * 构造下单请求事件消费者。
     *
     * @param orderCreateService 下单应用服务
     */
    public OrderCreateEventConsumer(OrderCreateService orderCreateService) {
        this.orderCreateService = orderCreateService;
    }

    /**
     * 消费下单请求事件。
     *
     * @param event 下单请求事件
     */
    @RabbitListener(queues = "${ticket.order.mq.create-queue}")
    public void consume(OrderCreateRequestedEvent event) {
        orderCreateService.handleOrderCreateRequested(event);
    }
}
