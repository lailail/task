package com.example.ticket.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 任务侧订单持久化对象。
 * 用于映射 `ticket_order` 表，为后续超时关闭任务提供最小订单查询字段。
 */
@TableName("ticket_order")
public class JobTicketOrderDO {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;
    @TableField("reservation_id")
    private String reservationId;
    @TableField("order_status")
    private String orderStatus;
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
