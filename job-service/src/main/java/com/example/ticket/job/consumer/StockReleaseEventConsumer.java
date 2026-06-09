package com.example.ticket.job.consumer;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.service.ReservationReleaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 库存释放事件消费者。
 * 用于接收统一的库存释放消息，并把真正的回补动作委托给释放服务处理。
 */
@Component
public class StockReleaseEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(StockReleaseEventConsumer.class);

    private final ReservationReleaseService reservationReleaseService;

    /**
     * 构造库存释放事件消费者。
     *
     * @param reservationReleaseService 预扣释放服务
     */
    public StockReleaseEventConsumer(ReservationReleaseService reservationReleaseService) {
        this.reservationReleaseService = reservationReleaseService;
    }

    /**
     * 消费库存释放事件。
     *
     * @param event 库存释放事件
     */
    @RabbitListener(queues = "${ticket.job.mq.stock-release-queue}")
    public void consume(StockReleaseEvent event) {
        log.info(
                "收到库存释放事件，eventId={}, reservationId={}, orderId={}, requestId={}, activityId={}, ticketId={}, userId={}",
                event.getEventId(),
                event.getReservationId(),
                event.getOrderId(),
                event.getRequestId(),
                event.getActivityId(),
                event.getTicketId(),
                event.getUserId()
        );
        reservationReleaseService.handleStockRelease(event);
        log.info("库存释放事件处理完成，eventId={}, reservationId={}, orderId={}", event.getEventId(), event.getReservationId(), event.getOrderId());
    }
}
