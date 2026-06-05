package com.example.ticket.order.support;

import com.example.ticket.common.event.order.OrderEventConstants;

/**
 * 订单模块常量。
 * 用于维护订单服务内部状态值、日志状态值和跨服务结果类型别名，避免业务字面量散落。
 */
public final class OrderConstants {
    public static final String ORDER_STATUS_CREATED = "CREATED";

    public static final String EVENT_CONSUME_STATUS_SUCCESS = "SUCCESS";
    public static final String EVENT_CONSUME_STATUS_FAILED = "FAILED";

    public static final String ORDER_RESULT_TYPE_CREATED = OrderEventConstants.ORDER_CREATED;
    public static final String ORDER_RESULT_TYPE_CREATE_FAILED = OrderEventConstants.ORDER_CREATE_FAILED;

    /**
     * 禁止实例化常量类。
     */
    private OrderConstants() {
    }
}
