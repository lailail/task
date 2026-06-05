package com.example.ticket.job.support;

/**
 * 任务服务 Redis Key 支持类。
 * 用于集中维护库存释放链路需要访问的 Redis Key 规则，避免补偿逻辑里散落硬编码。
 */
public final class JobRedisKeySupport {

    /**
     * 禁止实例化 Redis Key 支持类。
     */
    private JobRedisKeySupport() {
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
     * @param idempotencyKey 请求幂等键
     * @return 请求幂等 Key
     */
    public static String buildIdempotentKey(String idempotencyKey) {
        return "ticket:seckill:idempotent:" + idempotencyKey;
    }

    /**
     * 构造库存释放幂等 Key。
     *
     * @param reservationId 预扣标识
     * @return 库存释放幂等 Key
     */
    public static String buildReleaseKey(String reservationId) {
        return "ticket:stock:release:" + reservationId;
    }
}
