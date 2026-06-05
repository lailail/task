package com.example.ticket.job.service;

import com.example.ticket.common.event.order.OrderCreateResultEvent;

/**
 * 预扣确认服务。
 * 用于承接订单结果事件后的预扣状态收敛，避免订单服务直接同步更新预扣事实。
 */
public interface ReservationConfirmService {

    /**
     * 处理下单结果事件。
     *
     * @param event 下单结果事件
     */
    void handleOrderCreateResult(OrderCreateResultEvent event);
}
