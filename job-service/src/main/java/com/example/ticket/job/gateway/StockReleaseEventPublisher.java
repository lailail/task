package com.example.ticket.job.gateway;

import com.example.ticket.common.event.stock.StockReleaseEvent;

/**
 * 库存释放事件发布网关。
 * 用于把需要回补库存的补偿动作异步发送到统一释放总线。
 */
public interface StockReleaseEventPublisher {

    /**
     * 发布库存释放事件。
     *
     * @param event 库存释放事件
     */
    void publish(StockReleaseEvent event);
}
