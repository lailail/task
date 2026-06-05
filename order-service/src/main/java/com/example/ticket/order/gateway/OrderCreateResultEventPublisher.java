package com.example.ticket.order.gateway;

import com.example.ticket.common.event.order.OrderCreateResultEvent;

/**
 * 下单结果事件发布网关。
 * 用于隔离订单应用服务与 MQ 发布实现，便于后续替换发布方式或补充审计逻辑。
 */
public interface OrderCreateResultEventPublisher {

    /**
     * 发布下单结果事件。
     *
     * @param event 下单结果事件
     */
    void publish(OrderCreateResultEvent event);
}
