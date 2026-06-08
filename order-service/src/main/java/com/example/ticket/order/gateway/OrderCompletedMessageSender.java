package com.example.ticket.order.gateway;

import com.example.ticket.common.event.order.OrderCompletedEvent;
import com.example.ticket.common.reliable.ReliableMessageSender;

/**
 * 订单完成消息发送器。
 * 用于屏蔽具体 MQ 发送实现，让可靠消息模板只依赖统一发送抽象。
 */
public interface OrderCompletedMessageSender extends ReliableMessageSender<OrderCompletedEvent> {

    /**
     * 发送订单完成事件。
     *
     * @param event 订单完成事件
     */
    @Override
    void send(OrderCompletedEvent event);
}
