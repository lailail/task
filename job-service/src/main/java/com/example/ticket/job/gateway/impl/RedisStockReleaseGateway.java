package com.example.ticket.job.gateway.impl;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.gateway.StockReleaseGateway;
import com.example.ticket.job.support.JobConstants;
import com.example.ticket.job.support.JobRedisKeySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis 库存回补网关实现。
 * 用于通过外置 Lua 脚本原子完成库存加回、防重标记清理和释放幂等控制。
 */
@Component
public class RedisStockReleaseGateway implements StockReleaseGateway {
    private static final Logger log = LoggerFactory.getLogger(RedisStockReleaseGateway.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> releaseScript;
    private final long stockReleaseIdempotentSeconds;

    /**
     * 构造 Redis 库存回补网关。
     *
     * @param stringRedisTemplate Redis 字符串模板
     * @param releaseScript 库存释放脚本
     * @param stockReleaseIdempotentSeconds 释放幂等标记保留秒数
     */
    public RedisStockReleaseGateway(
            StringRedisTemplate stringRedisTemplate,
            @Qualifier("releaseStockRedisScript") DefaultRedisScript<List> releaseScript,
            @org.springframework.beans.factory.annotation.Value("${ticket.job.stock-release-idempotent-seconds:86400}")
            long stockReleaseIdempotentSeconds
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.releaseScript = releaseScript;
        this.stockReleaseIdempotentSeconds = stockReleaseIdempotentSeconds;
    }

    /**
     * 执行库存回补。
     *
     * @param event 库存释放事件
     * @return 是否完成本次实际释放
     */
    @Override
    public boolean release(StockReleaseEvent event) {
        List<String> keys = List.of(
                JobRedisKeySupport.buildStockKey(event.getActivityId(), event.getTicketId()),
                JobRedisKeySupport.buildUserOrderKey(event.getActivityId(), event.getUserId()),
                JobRedisKeySupport.buildReservationKey(event.getReservationId()),
                JobRedisKeySupport.buildReleaseKey(event.getReservationId()),
                JobRedisKeySupport.buildIdempotentKey(event.getIdempotencyKey())
        );
        List result = stringRedisTemplate.execute(
                releaseScript,
                keys,
                String.valueOf(event.getQuantity()),
                JobConstants.RESERVATION_STATUS_RELEASED,
                event.getReason(),
                String.valueOf(stockReleaseIdempotentSeconds)
        );
        if (result == null || result.isEmpty()) {
            log.error("Redis 库存回补脚本返回空结果，reservationId={}, orderId={}, requestId={}, activityId={}, ticketId={}", event.getReservationId(), event.getOrderId(), event.getRequestId(), event.getActivityId(), event.getTicketId());
            return false;
        }
        String resultCode = String.valueOf(result.get(0));
        log.info(
                "Redis 库存回补脚本执行完成，reservationId={}, orderId={}, requestId={}, activityId={}, ticketId={}, resultCode={}",
                event.getReservationId(),
                event.getOrderId(),
                event.getRequestId(),
                event.getActivityId(),
                event.getTicketId(),
                resultCode
        );
        return JobConstants.STOCK_RELEASE_RESULT_SUCCESS.equals(resultCode)
                || JobConstants.STOCK_RELEASE_RESULT_ALREADY_RELEASED.equals(resultCode);
    }
}
