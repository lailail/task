package com.example.ticket.order.support;

import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.common.reliable.ReliableMessageTaskStatus;

/**
 * 订单模块常量。
 * 用于维护订单服务内部状态值、日志状态值和跨服务结果类型别名，避免业务字面量散落。
 */
public final class OrderConstants {
    public static final String ORDER_STATUS_CREATED = "CREATED";
    public static final String ORDER_STATUS_PAID = "PAID";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String ORDER_STATUS_CLOSED = "CLOSED";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_SOURCE_ORDER_SERVICE = "ORDER_SERVICE";

    public static final String EVENT_CONSUME_STATUS_SUCCESS = "SUCCESS";
    public static final String EVENT_CONSUME_STATUS_FAILED = "FAILED";
    public static final String ORDER_RESULT_TASK_STATUS_PENDING = ReliableMessageTaskStatus.PENDING;
    public static final String ORDER_RESULT_TASK_STATUS_RETRYING = ReliableMessageTaskStatus.RETRYING;
    public static final String ORDER_RESULT_TASK_STATUS_SENT = ReliableMessageTaskStatus.SENT;
    public static final String ORDER_RESULT_TASK_STATUS_EXHAUSTED = ReliableMessageTaskStatus.EXHAUSTED;
    public static final String ORDER_COMPLETE_TASK_STATUS_PENDING = ReliableMessageTaskStatus.PENDING;
    public static final String ORDER_COMPLETE_TASK_STATUS_RETRYING = ReliableMessageTaskStatus.RETRYING;
    public static final String ORDER_COMPLETE_TASK_STATUS_SENT = ReliableMessageTaskStatus.SENT;
    public static final String ORDER_COMPLETE_TASK_STATUS_EXHAUSTED = ReliableMessageTaskStatus.EXHAUSTED;

    public static final String ORDER_RESULT_TYPE_CREATED = OrderEventConstants.ORDER_CREATED;
    public static final String ORDER_RESULT_TYPE_CREATE_FAILED = OrderEventConstants.ORDER_CREATE_FAILED;
    public static final String ORDER_RESULT_TYPE_COMPLETED = OrderEventConstants.ORDER_COMPLETED;
    public static final long DEFAULT_USER_ORDER_PAGE_NO = 1L;
    public static final long DEFAULT_USER_ORDER_PAGE_SIZE = 10L;
    public static final long MAX_USER_ORDER_PAGE_SIZE = 50L;

    public static final String USER_RESULT_STATUS_RESERVED = "RESERVED";
    public static final String USER_RESULT_STATUS_ORDER_CREATED = "ORDER_CREATED";
    public static final String USER_RESULT_STATUS_ORDER_PAID = "ORDER_PAID";
    public static final String USER_RESULT_STATUS_ORDER_COMPLETED = "ORDER_COMPLETED";
    public static final String USER_RESULT_STATUS_ORDER_CLOSED = "ORDER_CLOSED";
    public static final String USER_RESULT_STATUS_ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String USER_RESULT_STATUS_NOT_FOUND = "NOT_FOUND";

    /**
     * 禁止实例化常量类。
     */
    private OrderConstants() {
    }
}
