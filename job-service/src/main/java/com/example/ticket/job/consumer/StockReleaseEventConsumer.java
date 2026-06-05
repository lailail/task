package com.example.ticket.job.consumer;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.service.ReservationReleaseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 库存释放事件消费者。
 * 用于接收统一的库存释放消息，并把真正的回补动作委托给释放服务处理。
 */
@Component
public class StockReleaseEventConsumer {
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
        reservationReleaseService.handleStockRelease(event);
    }
}
