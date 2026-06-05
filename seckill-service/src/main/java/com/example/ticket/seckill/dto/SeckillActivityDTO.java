package com.example.ticket.seckill.dto;

/**
 * 抢票活动传输对象。
 * 用于在仓储层和服务层之间传递抢票活动的最小必要信息。
 */
public class SeckillActivityDTO {
    private Long activityId;
    private Long ticketId;
    private String activityName;
    private String saleStatus;
    private Integer availableStock;

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
     * 获取活动名称。
     *
     * @return 活动名称
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * 设置活动名称。
     *
     * @param activityName 活动名称
     */
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    /**
     * 获取销售状态。
     *
     * @return 销售状态
     */
    public String getSaleStatus() {
        return saleStatus;
    }

    /**
     * 设置销售状态。
     *
     * @param saleStatus 销售状态
     */
    public void setSaleStatus(String saleStatus) {
        this.saleStatus = saleStatus;
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
