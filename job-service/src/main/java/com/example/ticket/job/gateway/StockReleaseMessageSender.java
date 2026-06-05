package com.example.ticket.job.gateway;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.common.reliable.ReliableMessageSender;

/**
 * 库存释放消息发送器。
 * 用于屏蔽具体 MQ 发送实现，让可靠消息模板只依赖统一发送接口而不依赖具体基础设施。
 */
public interface StockReleaseMessageSender extends ReliableMessageSender<StockReleaseEvent> {

    /**
     * 发送库存释放事件。
     *
     * @param event 库存释放事件
     */
    @Override
    void send(StockReleaseEvent event);
}
