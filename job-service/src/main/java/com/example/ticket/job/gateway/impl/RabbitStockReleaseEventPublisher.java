package com.example.ticket.job.gateway.impl;

import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.gateway.StockReleaseMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 库存释放事件发布实现。
 * 用于把失败建单或后续关单补偿产生的释放动作发送给库存回补处理方。
 */
@Component
public class RabbitStockReleaseEventPublisher implements StockReleaseMessageSender {
    private static final Logger log = LoggerFactory.getLogger(RabbitStockReleaseEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造库存释放事件发布器。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public RabbitStockReleaseEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布库存释放事件。
     *
     * @param event 库存释放事件
     */
    @Override
    public void send(StockReleaseEvent event) {
        log.debug(
                "发送库存释放事件到 RabbitMQ，eventId={}, reservationId={}, orderId={}, requestId={}",
                event.getEventId(),
                event.getReservationId(),
                event.getOrderId(),
                event.getRequestId()
        );
        rabbitTemplate.convertAndSend(
                StockEventConstants.STOCK_RELEASE_EXCHANGE,
                StockEventConstants.STOCK_RELEASE_ROUTING_KEY,
                event
        );
    }
}
