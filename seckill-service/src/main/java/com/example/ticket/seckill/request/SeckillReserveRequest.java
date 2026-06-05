package com.example.ticket.seckill.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 抢票预扣请求对象。
 * 用于承载抢票入口的核心请求参数和最小校验约束。
 */
public class SeckillReserveRequest {
    @NotBlank
    private String requestId;

    @NotBlank
    private String idempotencyKey;

    @NotNull
    private Long activityId;

    @NotNull
    private Long ticketId;

    @NotNull
    @Min(1)
    private Integer quantity;

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
}
