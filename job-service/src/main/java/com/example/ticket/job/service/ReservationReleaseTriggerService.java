package com.example.ticket.job.service;

import com.example.ticket.common.event.order.OrderCreateResultEvent;

/**
 * 预扣释放触发服务。
 * 用于识别需要释放库存的失败建单结果，并把补偿动作转成统一的库存释放事件。
 */
public interface ReservationReleaseTriggerService {

    /**
     * 处理下单结果事件。
     *
     * @param event 下单结果事件
     */
    void handleOrderCreateResult(OrderCreateResultEvent event);
}
