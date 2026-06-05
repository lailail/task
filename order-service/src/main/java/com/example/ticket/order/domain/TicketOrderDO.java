package com.example.ticket.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 订单持久化对象。
 * 用于映射 `ticket_order` 表，为后续异步下单和订单状态流转提供数据库事实载体。
 */
@TableName("ticket_order")
public class TicketOrderDO {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;
    @TableField("order_no")
    private String orderNo;
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
    @TableField("amount_cent")
    private Integer amountCent;
    @TableField("order_status")
    private String orderStatus;
    private String source;
    @TableField("expire_at")
    private LocalDateTime expireAt;

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
     * 获取订单编号。
     *
     * @return 订单编号
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * 设置订单编号。
     *
     * @param orderNo 订单编号
     */
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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
     * 获取订单金额。
     *
     * @return 订单金额
     */
    public Integer getAmountCent() {
        return amountCent;
    }

    /**
     * 设置订单金额。
     *
     * @param amountCent 订单金额
     */
    public void setAmountCent(Integer amountCent) {
        this.amountCent = amountCent;
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
     * 获取过期时间。
     *
     * @return 过期时间
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /**
     * 设置过期时间。
     *
     * @param expireAt 过期时间
     */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
