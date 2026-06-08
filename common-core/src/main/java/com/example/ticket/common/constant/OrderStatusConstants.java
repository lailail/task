package com.example.ticket.common.constant;

/**
 * 订单状态公共常量。
 * 用于给多个服务共享订单主状态语义，避免出现跨模块反向依赖。
 */
public final class OrderStatusConstants {
    public static final String CREATED = "CREATED";
    public static final String PAID = "PAID";
    public static final String CANCELLED = "CANCELLED";
    public static final String CLOSED = "CLOSED";
    public static final String COMPLETED = "COMPLETED";

    /**
     * 禁止实例化常量类。
     */
    private OrderStatusConstants() {
    }
}
