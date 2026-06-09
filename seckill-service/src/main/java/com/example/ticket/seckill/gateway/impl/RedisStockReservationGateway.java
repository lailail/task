package com.example.ticket.seckill.gateway.impl;

import com.example.ticket.seckill.gateway.StockReservationGateway;
import com.example.ticket.seckill.gateway.model.StockRollbackResult;
import com.example.ticket.seckill.gateway.model.StockReserveCommand;
import com.example.ticket.seckill.gateway.model.StockReserveResult;
import com.example.ticket.seckill.support.SeckillConstants;
import com.example.ticket.seckill.support.SeckillRedisKeySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(RedisStockReservationGateway.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> reserveScript;
    private final DefaultRedisScript<List> rollbackScript;

    /**
     * 构造 Redis 库存预扣网关。
     *
     * @param stringRedisTemplate Redis 字符串模板
     * @param reserveScript 库存预扣脚本
     */
    public RedisStockReservationGateway(
            StringRedisTemplate stringRedisTemplate,
            @Qualifier("reserveStockRedisScript") DefaultRedisScript<List> reserveScript,
            @Qualifier("rollbackReserveStockRedisScript") DefaultRedisScript<List> rollbackScript
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.reserveScript = reserveScript;
        this.rollbackScript = rollbackScript;
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
        if (result == null || result.isEmpty()) {
            log.error(
                    "Redis 预扣脚本返回空结果，requestId={}, userId={}, activityId={}, ticketId={}, reservationId={}, idempotencyKey={}",
                    command.getRequestId(),
                    command.getUserId(),
                    command.getActivityId(),
                    command.getTicketId(),
                    command.getReservationId(),
                    command.getIdempotencyKey()
            );
            return StockReserveResult.failure(SeckillConstants.RESERVE_RESULT_FAILED, null);
        }

        String resultCode = String.valueOf(result.get(0));
        log.info(
                "Redis 预扣脚本执行完成，requestId={}, userId={}, activityId={}, ticketId={}, reservationId={}, idempotencyKey={}, resultCode={}",
                command.getRequestId(),
                command.getUserId(),
                command.getActivityId(),
                command.getTicketId(),
                command.getReservationId(),
                command.getIdempotencyKey(),
                resultCode
        );
        if (SeckillConstants.RESERVE_RESULT_SUCCESS.equals(resultCode)) {
            Instant expireAt = Instant.ofEpochSecond(Long.parseLong(String.valueOf(result.get(1))));
            return StockReserveResult.success(command.getReservationId(), Instant.now(), expireAt);
        }
        return StockReserveResult.failure(resultCode, null);
    }

    /**
     * 在正式预扣记录落库失败时回滚 Redis 预扣。
     *
     * @param command 预扣命令
     * @return 回滚结果
     */
    @Override
    public StockRollbackResult rollbackReservation(StockReserveCommand command) {
        List<String> keys = List.of(
                SeckillRedisKeySupport.buildStockKey(command.getActivityId(), command.getTicketId()),
                SeckillRedisKeySupport.buildUserOrderKey(command.getActivityId(), command.getUserId()),
                SeckillRedisKeySupport.buildReservationKey(command.getReservationId()),
                SeckillRedisKeySupport.buildIdempotentKey(command.getIdempotencyKey())
        );

        List result = stringRedisTemplate.execute(
                rollbackScript,
                keys,
                String.valueOf(command.getQuantity()),
                command.getReservationId()
        );
        if (result == null || result.isEmpty()) {
            log.error(
                    "Redis 预扣回滚脚本返回空结果，requestId={}, userId={}, activityId={}, ticketId={}, reservationId={}, idempotencyKey={}",
                    command.getRequestId(),
                    command.getUserId(),
                    command.getActivityId(),
                    command.getTicketId(),
                    command.getReservationId(),
                    command.getIdempotencyKey()
            );
            return StockRollbackResult.of(SeckillConstants.ROLLBACK_RESULT_FAILED);
        }
        String resultCode = String.valueOf(result.get(0));
        log.info(
                "Redis 预扣回滚脚本执行完成，requestId={}, userId={}, activityId={}, ticketId={}, reservationId={}, idempotencyKey={}, resultCode={}",
                command.getRequestId(),
                command.getUserId(),
                command.getActivityId(),
                command.getTicketId(),
                command.getReservationId(),
                command.getIdempotencyKey(),
                resultCode
        );
        return StockRollbackResult.of(resultCode);
    }
}
