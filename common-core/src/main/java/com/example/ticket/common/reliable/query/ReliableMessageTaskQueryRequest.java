package com.example.ticket.common.reliable.query;

/**
 * 补偿任务分页查询请求。
 * 用于承接后台治理页对可靠消息补偿任务的统一筛选条件和分页参数。
 */
public class ReliableMessageTaskQueryRequest {
    private String taskStatus;
    private String businessKey;
    private String eventKey;
    private Long current;
    private Long pageSize;

    /**
     * 获取任务状态筛选项。
     *
     * @return 任务状态
     */
    public String getTaskStatus() {
        return taskStatus;
    }

    /**
     * 设置任务状态筛选项。
     *
     * @param taskStatus 任务状态
     */
    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    /**
     * 获取业务主键筛选项。
     *
     * @return 业务主键
     */
    public String getBusinessKey() {
        return businessKey;
    }

    /**
     * 设置业务主键筛选项。
     *
     * @param businessKey 业务主键
     */
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    /**
     * 获取事件键筛选项。
     *
     * @return 事件键
     */
    public String getEventKey() {
        return eventKey;
    }

    /**
     * 设置事件键筛选项。
     *
     * @param eventKey 事件键
     */
    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

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
