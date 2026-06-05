package com.example.ticket.ticket.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 票种持久化对象。
 * 用于映射 `ticket_item` 表中的票种与库存展示字段。
 */
@TableName("ticket_item")
public class TicketItemDO {
    @TableId(value = "ticket_id", type = IdType.INPUT)
    private Long ticketId;
    @TableField("activity_id")
    private Long activityId;
    @TableField("ticket_name")
    private String ticketName;
    @TableField("price_cent")
    private Integer price;
    @TableField("available_stock")
    private Integer availableStock;

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
     * 获取所属活动标识。
     *
     * @return 所属活动标识
     */
    public Long getActivityId() {
        return activityId;
    }

    /**
     * 设置所属活动标识。
     *
     * @param activityId 所属活动标识
     */
    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    /**
     * 获取票种名称。
     *
     * @return 票种名称
     */
    public String getTicketName() {
        return ticketName;
    }

    /**
     * 设置票种名称。
     *
     * @param ticketName 票种名称
     */
    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    /**
     * 获取票价。
     *
     * @return 票价
     */
    public Integer getPrice() {
        return price;
    }

    /**
     * 设置票价。
     *
     * @param price 票价
     */
    public void setPrice(Integer price) {
        this.price = price;
    }

    /**
     * 获取可售库存。
     *
     * @return 可售库存
     */
    public Integer getAvailableStock() {
        return availableStock;
    }

    /**
     * 设置可售库存。
     *
     * @param availableStock 可售库存
     */
    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
}
