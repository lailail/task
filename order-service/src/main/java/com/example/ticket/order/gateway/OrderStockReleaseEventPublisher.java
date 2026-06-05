package com.example.ticket.order.gateway;

import com.example.ticket.common.event.stock.StockReleaseEvent;

/**
 * 订单域库存释放事件发布器。
 * 用于把取消订单、支付失败、支付过期等需要释放库存的动作统一转成显式事件。
 */
public interface OrderStockReleaseEventPublisher {

    /**
     * 发布库存释放事件。
     *
     * @param event 库存释放事件
     */
    void publish(StockReleaseEvent event);
}
