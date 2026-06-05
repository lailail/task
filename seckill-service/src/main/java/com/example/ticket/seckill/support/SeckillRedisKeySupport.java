package com.example.ticket.seckill.support;

/**
 * 抢票 Redis Key 支持类。
 * 用于集中维护抢票链路使用的 Redis Key 拼接规则，减少业务硬编码散落。
 */
public final class SeckillRedisKeySupport {

    /**
     * 禁止实例化 Redis Key 支持类。
     */
    private SeckillRedisKeySupport() {
    }

    /**
     * 构造库存快路径 Key。
     *
     * @param activityId 活动标识
     * @param ticketId 票种标识
     * @return 库存快路径 Key
     */
    public static String buildStockKey(Long activityId, Long ticketId) {
        return "ticket:seckill:stock:" + activityId + ":" + ticketId;
    }

    /**
     * 构造用户活动维度防重 Key。
     *
     * @param activityId 活动标识
     * @param userId 用户标识
     * @return 用户活动维度防重 Key
     */
    public static String buildUserOrderKey(Long activityId, Long userId) {
        return "ticket:seckill:user-order:" + activityId + ":" + userId;
    }

    /**
     * 构造预扣记录 Key。
     *
     * @param reservationId 预扣标识
     * @return 预扣记录 Key
     */
    public static String buildReservationKey(String reservationId) {
        return "ticket:seckill:reservation:" + reservationId;
    }

    /**
     * 构造请求幂等 Key。
     *
     * @param idempotencyKey 幂等键
     * @return 请求幂等 Key
     */
    public static String buildIdempotentKey(String idempotencyKey) {
        return "ticket:seckill:idempotent:" + idempotencyKey;
    }
}
