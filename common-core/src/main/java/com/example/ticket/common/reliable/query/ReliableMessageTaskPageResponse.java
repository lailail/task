package com.example.ticket.common.reliable.query;

import java.util.List;

/**
 * 补偿任务分页响应对象。
 * 用于统一后台治理页的分页语义，避免每个服务自行定义不同字段名。
 */
public class ReliableMessageTaskPageResponse {
    private List<ReliableMessageTaskResponse> records;
    private long total;
    private long current;
    private long pageSize;

    /**
     * 获取当前页记录列表。
     *
     * @return 当前页记录列表
     */
    public List<ReliableMessageTaskResponse> getRecords() {
        return records;
    }

    /**
     * 设置当前页记录列表。
     *
     * @param records 当前页记录列表
     */
    public void setRecords(List<ReliableMessageTaskResponse> records) {
        this.records = records;
    }

    /**
     * 获取总记录数。
     *
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 设置总记录数。
     *
     * @param total 总记录数
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * 获取当前页码。
     *
     * @return 当前页码
     */
    public long getCurrent() {
        return current;
    }

    /**
     * 设置当前页码。
     *
     * @param current 当前页码
     */
    public void setCurrent(long current) {
        this.current = current;
    }

    /**
     * 获取分页大小。
     *
     * @return 分页大小
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 设置分页大小。
     *
     * @param pageSize 分页大小
     */
    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
