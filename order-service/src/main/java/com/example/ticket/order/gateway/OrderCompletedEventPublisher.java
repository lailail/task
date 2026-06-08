package com.example.ticket.order.gateway;

import com.example.ticket.common.event.order.OrderCompletedEvent;

/**
 * 订单完成事件发布器。
 * 用于在订单完成态落地后，向下游发布正式完成事件。
 */
public interface OrderCompletedEventPublisher {

    /**
     * 发布订单完成事件。
     *
     * @param event 订单完成事件
     */
    void publish(OrderCompletedEvent event);
}
