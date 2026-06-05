package com.example.ticket.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 任务侧订单持久化对象。
 * 用于映射 `ticket_order` 表，支撑超时关单扫描、状态推进和库存回补编排。
 */
@TableName("ticket_order")
public class JobTicketOrderDO {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    @TableField("reservation_id")
    private String reservationId;

    @TableField("request_id")
    private String requestId;

    @TableField("idempotency_key")
    private String idempotencyKey;

    @TableField("user_id")
    private Long userId;

    @TableField("activity_id")
    private Long activityId;

    @TableField("ticket_id")
    private Long ticketId;

    private Integer quantity;

    @TableField("order_status")
    private String orderStatus;

    @TableField("expire_at")
    private LocalDateTime expireAt;

    @TableField("closed_at")
    private LocalDateTime closedAt;

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
     * 获取预扣记录标识。
     *
     * @return 预扣记录标识
     */
    public String getReservationId() {
        return reservationId;
    }

    /**
     * 设置预扣记录标识。
     *
     * @param reservationId 预扣记录标识
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
     * 获取业务幂等键。
     *
     * @return 业务幂等键
     */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * 设置业务幂等键。
     *
     * @param idempotencyKey 业务幂等键
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
     * 获取购票数量。
     *
     * @return 购票数量
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * 设置购票数量。
     *
     * @param quantity 购票数量
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 获取订单状态。
     *
     * @return 订单状态
     */
    public String getOrderStatus() {
        return orderStatus;
    }

    /**
     * 设置订单状态。
     *
     * @param orderStatus 订单状态
     */
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * 获取订单过期时间。
     *
     * @return 订单过期时间
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /**
     * 设置订单过期时间。
     *
     * @param expireAt 订单过期时间
     */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    /**
     * 获取关单时间。
     *
     * @return 关单时间
     */
    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    /**
     * 设置关单时间。
     *
     * @param closedAt 关单时间
     */
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
