package com.example.ticket.ticket.response;

/**
 * 活动摘要响应对象。
 * 用于列表页返回活动的摘要展示字段。
 */
public class ActivitySummaryResponse {
    private Long activityId;
    private String activityName;
    private String city;
    private String venueName;
    private String saleStatus;

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
     * 获取城市名称。
     *
     * @return 城市名称
     */
    public String getCity() {
        return city;
    }

    /**
     * 设置城市名称。
     *
     * @param city 城市名称
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取场馆名称。
     *
     * @return 场馆名称
     */
    public String getVenueName() {
        return venueName;
    }

    /**
     * 设置场馆名称。
     *
     * @param venueName 场馆名称
     */
    public void setVenueName(String venueName) {
        this.venueName = venueName;
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
}
