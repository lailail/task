package com.example.ticket.job.support;

import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.common.reliable.ReliableMessageTaskStatus;

/**
 * 任务服务常量。
 * 用于集中维护预扣状态、失败释放和库存回补链路中的状态值、结果码与来源标识。
 */
public final class JobConstants {
    public static final String RESERVATION_STATUS_RESERVED = "RESERVED";
    public static final String RESERVATION_STATUS_CONFIRMED = "CONFIRMED";
    public static final String RESERVATION_STATUS_RELEASED = "RELEASED";
    public static final String ORDER_STATUS_CREATED = "CREATED";
    public static final String ORDER_STATUS_CLOSED = "CLOSED";

    public static final String ORDER_RESULT_TYPE_CREATED = OrderEventConstants.ORDER_CREATED;
    public static final String ORDER_RESULT_TYPE_CREATE_FAILED = OrderEventConstants.ORDER_CREATE_FAILED;

    public static final String SOURCE_JOB_SERVICE = "JOB_SERVICE";
    public static final String STOCK_RELEASE_REASON_ORDER_TIMEOUT = "ORDER_TIMEOUT";
    public static final String STOCK_RELEASE_REASON_RESERVATION_RECHECK_EXPIRED = "RESERVATION_RECHECK_EXPIRED";

    public static final String STOCK_RELEASE_RESULT_SUCCESS = "SUCCESS";
    public static final String STOCK_RELEASE_RESULT_ALREADY_RELEASED = "ALREADY_RELEASED";
    public static final String STOCK_RELEASE_TASK_STATUS_PENDING = ReliableMessageTaskStatus.PENDING;
    public static final String STOCK_RELEASE_TASK_STATUS_RETRYING = ReliableMessageTaskStatus.RETRYING;
    public static final String STOCK_RELEASE_TASK_STATUS_SENT = ReliableMessageTaskStatus.SENT;
    public static final String STOCK_RELEASE_TASK_STATUS_EXHAUSTED = ReliableMessageTaskStatus.EXHAUSTED;

    /**
     * 禁止实例化常量类。
     */
    private JobConstants() {
    }
}
