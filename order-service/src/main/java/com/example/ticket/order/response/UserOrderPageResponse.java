package com.example.ticket.order.response;

import java.util.List;

/**
 * 用户订单分页响应。
 * 用于前台“我的订单”页面承接当前用户自己的订单列表。
 */
public class UserOrderPageResponse {
    private Long current;
    private Long pageSize;
    private Long total;
    private List<UserOrderResponse> records;

    /** @return 当前页码 */
    public Long getCurrent() { return current; }
    /** @param current 当前页码 */
    public void setCurrent(Long current) { this.current = current; }
    /** @return 分页大小 */
    public Long getPageSize() { return pageSize; }
    /** @param pageSize 分页大小 */
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
    /** @return 总记录数 */
    public Long getTotal() { return total; }
    /** @param total 总记录数 */
    public void setTotal(Long total) { this.total = total; }
    /** @return 当前页订单记录 */
    public List<UserOrderResponse> getRecords() { return records; }
    /** @param records 当前页订单记录 */
    public void setRecords(List<UserOrderResponse> records) { this.records = records; }
}
