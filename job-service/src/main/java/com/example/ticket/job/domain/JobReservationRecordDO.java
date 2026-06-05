package com.example.ticket.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 预扣记录持久化对象。
 * 用于映射 `stock_reservation_record` 表，支撑预扣确认、库存释放和回查补偿任务。
 */
@TableName("stock_reservation_record")
public class JobReservationRecordDO {
    @TableId(value = "reservation_id", type = IdType.INPUT)
    private String reservationId;

    @TableField("request_id")
    private String requestId;

    @TableField("idempotency_key")
    private String idempotencyKey;

    @TableField("user_id")
    private Long userId;

    @TableField("order_id")
    private Long orderId;

    @TableField("activity_id")
    private Long activityId;

    @TableField("ticket_id")
    private Long ticketId;

    private Integer quantity;

    @TableField("reservation_status")
    private String reservationStatus;

    @TableField("reason")
    private String reason;

    private String source;

    @TableField("expire_at")
    private LocalDateTime expireAt;

    @TableField("released_at")
    private LocalDateTime releasedAt;

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
     * 获取订单主键。
     *
     * @return 订单主键
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * 设置订单主键。
     *
     * @param orderId 订单主键
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
    public String getReservationStatus() {
        return reservationStatus;
    }

    /**
     * 设置预扣状态。
     *
     * @param reservationStatus 预扣状态
     */
    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    /**
     * 获取释放或失败原因。
     *
     * @return 释放或失败原因
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置释放或失败原因。
     *
     * @param reason 释放或失败原因
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取来源服务。
     *
     * @return 来源服务
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置来源服务。
     *
     * @param source 来源服务
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取预扣过期时间。
     *
     * @return 预扣过期时间
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /**
     * 设置预扣过期时间。
     *
     * @param expireAt 预扣过期时间
     */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    /**
     * 获取释放时间。
     *
     * @return 释放时间
     */
    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    /**
     * 设置释放时间。
     *
     * @param releasedAt 释放时间
     */
    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }
}
