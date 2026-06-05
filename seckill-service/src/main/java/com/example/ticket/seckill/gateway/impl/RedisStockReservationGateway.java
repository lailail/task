package com.example.ticket.seckill.gateway.impl;

import com.example.ticket.seckill.gateway.StockReservationGateway;
import com.example.ticket.seckill.gateway.model.StockReserveCommand;
import com.example.ticket.seckill.gateway.model.StockReserveResult;
import com.example.ticket.seckill.support.SeckillConstants;
import com.example.ticket.seckill.support.SeckillRedisKeySupport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Redis Lua 库存预扣实现。
 * 当前通过一个 Lua 脚本原子完成幂等判断、防重判断、库存扣减和预扣记录写入。
 */
@Component
public class RedisStockReservationGateway implements StockReservationGateway {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> reserveScript;

    /**
     * 构造 Redis 库存预扣网关。
     *
     * @param stringRedisTemplate Redis 字符串模板
     * @param reserveScript 库存预扣脚本
     */
    public RedisStockReservationGateway(
            StringRedisTemplate stringRedisTemplate,
            @Qualifier("reserveStockRedisScript") DefaultRedisScript<List> reserveScript
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.reserveScript = reserveScript;
    }

    /**
     * 执行库存预扣。
     *
     * @param command 预扣命令
     * @return 预扣结果
     */
    @Override
    public StockReserveResult reserve(StockReserveCommand command) {
        List<String> keys = List.of(
                SeckillRedisKeySupport.buildStockKey(command.getActivityId(), command.getTicketId()),
                SeckillRedisKeySupport.buildUserOrderKey(command.getActivityId(), command.getUserId()),
                SeckillRedisKeySupport.buildReservationKey(command.getReservationId()),
                SeckillRedisKeySupport.buildIdempotentKey(command.getIdempotencyKey())
        );

        List result = stringRedisTemplate.execute(
                reserveScript,
                keys,
                String.valueOf(command.getQuantity()),
                command.getReservationId(),
                String.valueOf(command.getActivityId()),
                String.valueOf(command.getTicketId()),
                String.valueOf(command.getUserId()),
                command.getStatus(),
                String.valueOf(command.getExpireAtEpochSecond()),
                command.getIdempotencyKey(),
                command.getRequestId(),
                String.valueOf(command.getExpireSeconds())
        );

        String resultCode = String.valueOf(result.get(0));
        if (SeckillConstants.RESERVE_RESULT_SUCCESS.equals(resultCode)) {
            Instant expireAt = Instant.ofEpochSecond(Long.parseLong(String.valueOf(result.get(1))));
            return StockReserveResult.success(command.getReservationId(), Instant.now(), expireAt);
        }
        return StockReserveResult.failure(resultCode, null);
    }
}
