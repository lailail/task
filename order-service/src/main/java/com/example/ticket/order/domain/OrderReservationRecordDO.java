package com.example.ticket.order.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 订单域只读预扣记录对象。
 * 用于用户侧结果感知查询，读取 `stock_reservation_record` 的最小字段以判断预扣是否属于当前用户。
 */
@TableName("stock_reservation_record")
public class OrderReservationRecordDO {
    @TableId("reservation_id")
    private String reservationId;
    @TableField("user_id")
    private Long userId;
    @TableField("activity_id")
    private Long activityId;
    @TableField("ticket_id")
    private Long ticketId;
    private Integer quantity;
    @TableField("reservation_status")
    private String reservationStatus;
    @TableField("expire_at")
    private LocalDateTime expireAt;

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
}
