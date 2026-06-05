package com.example.ticket.seckill.gateway.model;

/**
 * 库存预扣命令对象。
 * 用于把服务层需要的预扣上下文统一传递给缓存网关。
 */
public class StockReserveCommand {
    private String reservationId;
    private String requestId;
    private String idempotencyKey;
    private Long userId;
    private Long activityId;
    private Long ticketId;
    private Integer quantity;
    private String status;
    private long expireSeconds;
    private long expireAtEpochSecond;

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
     * 获取请求标识。
     *
     * @return 请求标识
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 设置请求标识。
     *
     * @param requestId 请求标识
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取幂等键。
     *
     * @return 幂等键
     */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * 设置幂等键。
     *
     * @param idempotencyKey 幂等键
     */
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    /**
     * 获取用户标识。
     *
     * @return 用户标识
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户标识。
     *
     * @param userId 用户标识
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取活动标识。
     *
     * @return 活动标识
     */
    public Long getActivityId() {
        return activityId;
    }

    /**
     * 设置活动标识。
     *
     * @param activityId 活动标识
     */
    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    /**
     * 获取票种标识。
     *
     * @return 票种标识
     */
    public Long getTicketId() {
        return ticketId;
    }

    /**
     * 设置票种标识。
     *
     * @param ticketId 票种标识
     */
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * 获取预扣数量。
     *
     * @return 预扣数量
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * 设置预扣数量。
     *
     * @param quantity 预扣数量
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
     * 获取过期秒数。
     *
     * @return 过期秒数
     */
    public long getExpireSeconds() {
        return expireSeconds;
    }

    /**
     * 设置过期秒数。
     *
     * @param expireSeconds 过期秒数
     */
    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    /**
     * 获取过期时间戳秒值。
     *
     * @return 过期时间戳秒值
     */
    public long getExpireAtEpochSecond() {
        return expireAtEpochSecond;
    }

    /**
     * 设置过期时间戳秒值。
     *
     * @param expireAtEpochSecond 过期时间戳秒值
     */
    public void setExpireAtEpochSecond(long expireAtEpochSecond) {
        this.expireAtEpochSecond = expireAtEpochSecond;
    }
}
