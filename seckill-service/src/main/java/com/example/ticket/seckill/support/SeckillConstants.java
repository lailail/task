package com.example.ticket.seckill.support;

/**
 * 抢票模块常量定义。
 * 用于集中管理当前阶段的状态值、结果码和消息路由，避免业务字面量散落。
 */
public final class SeckillConstants {

    public static final String SALE_STATUS_ON_SALE = "ON_SALE";
    public static final String SALE_STATUS_COMING_SOON = "COMING_SOON";

    public static final String RESERVATION_STATUS_RESERVED = "RESERVED";
    public static final String RESERVATION_STATUS_CONFIRMED = "CONFIRMED";
    public static final String RESERVATION_STATUS_RELEASED = "RELEASED";

    public static final String RESERVE_RESULT_SUCCESS = "SUCCESS";
    public static final String RESERVE_RESULT_DUPLICATE = "DUPLICATE";
    public static final String RESERVE_RESULT_OUT_OF_STOCK = "OUT_OF_STOCK";

    public static final String ORDER_CREATE_EVENT_TYPE = "ORDER_CREATE_REQUESTED";
    public static final String ORDER_CREATE_EVENT_SOURCE = "SECKILL_SERVICE";
    public static final String ORDER_CREATE_EXCHANGE = "ticket.order.exchange";
    public static final String ORDER_CREATE_ROUTING_KEY = "ticket.order.create";

    /**
     * 禁止实例化常量类。
     */
    private SeckillConstants() {
    }
}
