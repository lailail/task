package com.example.ticket.job.consumer;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.job.service.ReservationConfirmService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单结果事件消费者。
 * 用于接收订单服务发出的结果事件，并委托预扣确认服务推进状态收敛。
 */
@Component
public class OrderResultEventConsumer {
    private final ReservationConfirmService reservationConfirmService;

    /**
     * 构造订单结果事件消费者。
     *
     * @param reservationConfirmService 预扣确认服务
     */
    public OrderResultEventConsumer(ReservationConfirmService reservationConfirmService) {
        this.reservationConfirmService = reservationConfirmService;
    }

    /**
     * 消费订单结果事件。
     *
     * @param event 订单结果事件
     */
    @RabbitListener(queues = "${ticket.job.mq.order-result-queue}")
    public void consume(OrderCreateResultEvent event) {
        reservationConfirmService.handleOrderCreateResult(event);
    }
}
