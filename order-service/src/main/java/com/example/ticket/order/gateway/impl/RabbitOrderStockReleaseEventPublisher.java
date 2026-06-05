package com.example.ticket.order.gateway.impl;

import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.order.gateway.OrderStockReleaseEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 库存释放事件发布器。
 * 用于把订单域中的取消或支付失败事实异步通知给库存释放链路。
 */
@Component
public class RabbitOrderStockReleaseEventPublisher implements OrderStockReleaseEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造库存释放事件发布器。
     *
     * @param rabbitTemplate RabbitTemplate
     */
    public RabbitOrderStockReleaseEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布库存释放事件。
     *
     * @param event 库存释放事件
     */
    @Override
    public void publish(StockReleaseEvent event) {
        rabbitTemplate.convertAndSend(
                StockEventConstants.STOCK_RELEASE_EXCHANGE,
                StockEventConstants.STOCK_RELEASE_ROUTING_KEY,
                event
        );
    }
}
