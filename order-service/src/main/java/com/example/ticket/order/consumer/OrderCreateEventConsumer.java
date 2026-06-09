package com.example.ticket.order.consumer;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.order.service.OrderCreateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 下单请求事件消费者。
 * 用于承接 `ticket.order.create` 消息，并把实际业务处理委托给下单应用服务。
 */
@Component
public class OrderCreateEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderCreateEventConsumer.class);

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
        log.info(
                "收到下单请求事件，eventId={}, requestId={}, reservationId={}, activityId={}, ticketId={}, userId={}, idempotencyKey={}",
                event.getEventId(),
                event.getRequestId(),
                event.getReservationId(),
                event.getActivityId(),
                event.getTicketId(),
                event.getUserId(),
                event.getIdempotencyKey()
        );
        orderCreateService.handleOrderCreateRequested(event);
        log.info("下单请求事件处理完成，eventId={}, reservationId={}, idempotencyKey={}", event.getEventId(), event.getReservationId(), event.getIdempotencyKey());
    }
}
