package com.example.ticket.order.gateway;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.reliable.ReliableMessageSender;

/**
 * 下单结果消息发送器。
 * 用于屏蔽具体 MQ 发送实现，让可靠消息模板只依赖统一发送接口而不依赖具体基础设施。
 */
public interface OrderCreateResultMessageSender extends ReliableMessageSender<OrderCreateResultEvent> {

    /**
     * 发送下单结果事件。
     *
     * @param event 下单结果事件
     */
    @Override
    void send(OrderCreateResultEvent event);
}
