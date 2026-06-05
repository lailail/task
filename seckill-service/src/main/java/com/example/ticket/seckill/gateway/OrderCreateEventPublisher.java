package com.example.ticket.seckill.gateway;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;

/**
 * 下单事件发布网关。
 * 用于隔离服务层与 MQ 发布实现。
 */
public interface OrderCreateEventPublisher {

    /**
     * 发布下单事件。
     *
     * @param event 下单事件
     */
    void publish(OrderCreateRequestedEvent event);
}
