package com.example.ticket.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 预扣记录持久化对象。
 * 用于映射 `stock_reservation_record` 表，为后续超时关闭和库存回补任务提供查询入口。
 */
@TableName("stock_reservation_record")
public class JobReservationRecordDO {
    @TableId(value = "reservation_id", type = IdType.INPUT)
    private String reservationId;
    @TableField("order_id")
    private Long orderId;
    @TableField("activity_id")
    private Long activityId;
    @TableField("ticket_id")
    private Long ticketId;
    @TableField("reservation_status")
    private String reservationStatus;

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
}
