package com.example.ticket.common.event.order;

/**
 * 订单事件公共常量。
 * 用于集中维护跨服务共享的订单消息类型、路由键和来源标识，避免契约字面量散落。
 */
public final class OrderEventConstants {
    public static final String ORDER_CREATE_EXCHANGE = "ticket.order.exchange";
    public static final String ORDER_CREATE_QUEUE = "ticket.order.create.queue";
    public static final String ORDER_CREATE_ROUTING_KEY = "ticket.order.create";

    public static final String ORDER_RESULT_QUEUE = "ticket.order.result.queue";
    public static final String ORDER_RESULT_ROUTING_KEY = "ticket.order.result";

    public static final String ORDER_CREATE_REQUESTED = "ORDER_CREATE_REQUESTED";
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_CREATE_FAILED = "ORDER_CREATE_FAILED";

    public static final String SOURCE_SECKILL_SERVICE = "SECKILL_SERVICE";
    public static final String SOURCE_ORDER_SERVICE = "ORDER_SERVICE";

    /**
     * 禁止实例化常量类。
     */
    private OrderEventConstants() {
    }
}
