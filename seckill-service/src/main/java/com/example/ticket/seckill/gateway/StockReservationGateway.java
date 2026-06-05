package com.example.ticket.seckill.gateway;

import com.example.ticket.seckill.gateway.model.StockReserveCommand;
import com.example.ticket.seckill.gateway.model.StockReserveResult;

/**
 * 库存预扣缓存网关。
 * 用于隔离服务层与 Redis Lua 预扣实现。
 */
public interface StockReservationGateway {

    /**
     * 执行库存预扣。
     *
     * @param command 预扣命令
     * @return 预扣结果
     */
    StockReserveResult reserve(StockReserveCommand command);
}
