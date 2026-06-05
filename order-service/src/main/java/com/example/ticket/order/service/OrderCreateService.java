package com.example.ticket.order.service;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;

/**
 * 下单创建应用服务。
 * 用于承接 `ticket.order.create` 事件后的建单编排、幂等控制和结果事件发布。
 */
public interface OrderCreateService {

    /**
     * 处理下单请求事件。
     *
     * @param event 下单请求事件
     */
    void handleOrderCreateRequested(OrderCreateRequestedEvent event);
}
