package com.example.ticket.seckill.response;

import java.time.Instant;

/**
 * 抢票预扣响应对象。
 * 用于向调用方返回当前阶段最小可用的预扣结果。
 */
public class SeckillReserveResponse {
    private String reservationId;
    private String status;
    private Instant expireAt;

    /**
     * 获取预扣标识。
     *
     * @return 预扣标识
     */
    public String getReservationId() {
        return reservationId;
    }

    /**
     * 设置预扣标识。
     *
     * @param reservationId 预扣标识
     */
    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * 获取预扣状态。
     *
     * @return 预扣状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置预扣状态。
     *
     * @param status 预扣状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取过期时间。
     *
     * @return 过期时间
     */
    public Instant getExpireAt() {
        return expireAt;
    }

    /**
     * 设置过期时间。
     *
     * @param expireAt 过期时间
     */
    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }
}
