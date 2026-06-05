package com.example.ticket.job.gateway;

import com.example.ticket.common.event.stock.StockReleaseEvent;

/**
 * 库存释放网关。
 * 用于隔离补偿服务与 Redis 回补脚本执行细节，避免释放编排直接依赖底层缓存命令。
 */
public interface StockReleaseGateway {

    /**
     * 执行库存回补。
     *
     * @param event 库存释放事件
     * @return 是否完成本次实际释放
     */
    boolean release(StockReleaseEvent event);
}
