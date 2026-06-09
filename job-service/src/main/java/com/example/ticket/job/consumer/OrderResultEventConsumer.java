package com.example.ticket.job.consumer;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.job.service.ReservationConfirmService;
import com.example.ticket.job.service.ReservationReleaseTriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单结果事件消费者。
 * 用于接收订单服务发出的建单结果，并分别委托成功确认与失败释放触发服务处理。
 */
@Component
public class OrderResultEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderResultEventConsumer.class);

    private final ReservationConfirmService reservationConfirmService;
    private final ReservationReleaseTriggerService reservationReleaseTriggerService;

    /**
     * 构造订单结果事件消费者。
     *
     * @param reservationConfirmService 预扣确认服务
     * @param reservationReleaseTriggerService 预扣释放触发服务
     */
    public OrderResultEventConsumer(
            ReservationConfirmService reservationConfirmService,
            ReservationReleaseTriggerService reservationReleaseTriggerService
    ) {
        this.reservationConfirmService = reservationConfirmService;
        this.reservationReleaseTriggerService = reservationReleaseTriggerService;
    }

    /**
     * 消费订单结果事件。
     *
     * @param event 订单结果事件
     */
    @RabbitListener(queues = "${ticket.job.mq.order-result-queue}")
    public void consume(OrderCreateResultEvent event) {
        log.info(
                "收到订单结果事件，eventId={}, eventType={}, reservationId={}, orderId={}, requestId={}, idempotencyKey={}",
                event.getEventId(),
                event.getEventType(),
                event.getReservationId(),
                event.getOrderId(),
                event.getRequestId(),
                event.getIdempotencyKey()
        );
        reservationConfirmService.handleOrderCreateResult(event);
        reservationReleaseTriggerService.handleOrderCreateResult(event);
        log.info("订单结果事件处理完成，eventId={}, reservationId={}, orderId={}", event.getEventId(), event.getReservationId(), event.getOrderId());
    }
}
