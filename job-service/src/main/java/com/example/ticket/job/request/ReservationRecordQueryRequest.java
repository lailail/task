package com.example.ticket.job.request;

/**
 * 预扣记录分页查询请求。
 * 用于承接后台治理页的筛选条件和分页参数，避免控制层直接处理查询细节。
 */
public class ReservationRecordQueryRequest {
    private String reservationId;
    private String requestId;
    private Long userId;
    private Long activityId;
    private Long ticketId;
    private String reservationStatus;
    private Long current;
    private Long pageSize;

    /** 获取预扣标识。 */
    public String getReservationId() {
        return reservationId;
    }

    /** 设置预扣标识。 */
    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    /** 获取请求标识。 */
    public String getRequestId() {
        return requestId;
    }

    /** 设置请求标识。 */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /** 获取用户标识。 */
    public Long getUserId() {
        return userId;
    }

    /** 设置用户标识。 */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** 获取活动标识。 */
    public Long getActivityId() {
        return activityId;
    }

    /** 设置活动标识。 */
    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    /** 获取票种标识。 */
    public Long getTicketId() {
        return ticketId;
    }

    /** 设置票种标识。 */
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    /** 获取预扣状态。 */
    public String getReservationStatus() {
        return reservationStatus;
    }

    /** 设置预扣状态。 */
    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    /** 获取当前页码。 */
    public Long getCurrent() {
        return current;
    }

    /** 设置当前页码。 */
    public void setCurrent(Long current) {
        this.current = current;
    }

    /** 获取分页大小。 */
    public Long getPageSize() {
        return pageSize;
    }

    /** 设置分页大小。 */
    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
