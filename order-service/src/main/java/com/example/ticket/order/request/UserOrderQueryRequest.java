package com.example.ticket.order.request;

/**
 * 用户订单分页查询请求。
 * 只承接分页参数，用户身份必须来自网关透传头，不能由前端传入。
 */
public class UserOrderQueryRequest {
    private Long current;
    private Long pageSize;

    /**
     * 获取当前页码。
     *
     * @return 当前页码
     */
    public Long getCurrent() {
        return current;
    }

    /**
     * 设置当前页码。
     *
     * @param current 当前页码
     */
    public void setCurrent(Long current) {
        this.current = current;
    }

    /**
     * 获取分页大小。
     *
     * @return 分页大小
     */
    public Long getPageSize() {
        return pageSize;
    }

    /**
     * 设置分页大小。
     *
     * @param pageSize 分页大小
     */
    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
