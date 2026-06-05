package com.example.ticket.seckill.gateway.model;

import com.example.ticket.seckill.support.SeckillConstants;

import java.time.Instant;

/**
 * 库存预扣结果对象。
 * 用于承接缓存网关返回的预扣结果与过期时间。
 */
public class StockReserveResult {
    private final String resultCode;
    private final String reservationId;
    private final Instant occurredAt;
    private final Instant expireAt;

    /**
     * 构造库存预扣结果对象。
     *
     * @param resultCode 预扣结果码
     * @param reservationId 预扣标识
     * @param occurredAt 结果发生时间
     * @param expireAt 预扣过期时间
     */
    private StockReserveResult(String resultCode, String reservationId, Instant occurredAt, Instant expireAt) {
        this.resultCode = resultCode;
        this.reservationId = reservationId;
        this.occurredAt = occurredAt;
        this.expireAt = expireAt;
    }

    /**
     * 构造预扣成功结果。
     *
     * @param reservationId 预扣标识
     * @param occurredAt 结果发生时间
     * @param expireAt 预扣过期时间
     * @return 成功结果
     */
    public static StockReserveResult success(String reservationId, Instant occurredAt, Instant expireAt) {
        return new StockReserveResult(SeckillConstants.RESERVE_RESULT_SUCCESS, reservationId, occurredAt, expireAt);
    }

    /**
     * 构造预扣失败结果。
     *
     * @param resultCode 失败结果码
     * @param expireAt 失败场景下可携带的过期时间
     * @return 失败结果
     */
    public static StockReserveResult failure(String resultCode, Instant expireAt) {
        return new StockReserveResult(resultCode, null, null, expireAt);
    }

    /**
     * 获取预扣结果码。
     *
     * @return 预扣结果码
     */
    public String getResultCode() {
        return resultCode;
    }

    /**
     * 获取预扣标识。
     *
     * @return 预扣标识
     */
    public String getReservationId() {
        return reservationId;
    }

    /**
     * 获取结果发生时间。
     *
     * @return 结果发生时间
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * 获取预扣过期时间。
     *
     * @return 预扣过期时间
     */
    public Instant getExpireAt() {
        return expireAt;
    }
}
