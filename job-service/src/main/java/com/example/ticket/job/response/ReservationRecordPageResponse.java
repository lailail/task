package com.example.ticket.job.response;

import java.util.List;

/**
 * 预扣记录分页响应对象。
 * 用于向后台页返回分页元数据和记录列表，避免前端自行推断分页语义。
 */
public class ReservationRecordPageResponse {
    private List<ReservationRecordResponse> records;
    private long total;
    private long current;
    private long pageSize;

    /** 获取当前页记录。 */
    public List<ReservationRecordResponse> getRecords() {
        return records;
    }

    /** 设置当前页记录。 */
    public void setRecords(List<ReservationRecordResponse> records) {
        this.records = records;
    }

    /** 获取总记录数。 */
    public long getTotal() {
        return total;
    }

    /** 设置总记录数。 */
    public void setTotal(long total) {
        this.total = total;
    }

    /** 获取当前页码。 */
    public long getCurrent() {
        return current;
    }

    /** 设置当前页码。 */
    public void setCurrent(long current) {
        this.current = current;
    }

    /** 获取分页大小。 */
    public long getPageSize() {
        return pageSize;
    }

    /** 设置分页大小。 */
    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
