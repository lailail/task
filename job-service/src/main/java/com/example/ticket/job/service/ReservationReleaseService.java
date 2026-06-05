package com.example.ticket.job.service;

import com.example.ticket.common.event.stock.StockReleaseEvent;

/**
 * 预扣释放服务。
 * 用于消费库存释放事件后执行 Redis 回补，并把预扣记录收敛到最终释放状态。
 */
public interface ReservationReleaseService {

    /**
     * 处理库存释放事件。
     *
     * @param event 库存释放事件
     */
    void handleStockRelease(StockReleaseEvent event);
}
