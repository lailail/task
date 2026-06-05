package com.example.ticket.job.support;

import com.example.ticket.common.event.order.OrderEventConstants;

/**
 * 任务服务常量。
 * 用于集中维护预扣状态收敛和订单结果事件处理所需的状态值与事件类型。
 */
public final class JobConstants {
    public static final String RESERVATION_STATUS_RESERVED = "RESERVED";
    public static final String RESERVATION_STATUS_CONFIRMED = "CONFIRMED";

    public static final String ORDER_RESULT_TYPE_CREATED = OrderEventConstants.ORDER_CREATED;
    public static final String ORDER_RESULT_TYPE_CREATE_FAILED = OrderEventConstants.ORDER_CREATE_FAILED;

    /**
     * 禁止实例化常量类。
     */
    private JobConstants() {
    }
}
