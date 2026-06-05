package com.example.ticket.seckill.gateway;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.reliable.ReliableMessageSender;

/**
 * 抢票侧下单请求消息发送器。
 * 用于屏蔽具体 MQ 发送实现，让可靠消息模板只依赖统一发送接口。
 */
public interface OrderCreateMessageSender extends ReliableMessageSender<OrderCreateRequestedEvent> {

    /**
     * 发送下单请求事件。
     *
     * @param event 下单请求事件
     */
    @Override
    void send(OrderCreateRequestedEvent event);
}
