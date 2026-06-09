package com.example.ticket.seckill.support;

import com.example.ticket.common.reliable.ReliableMessageTaskStatus;

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
    public static final String RESERVATION_SOURCE_SECKILL_SERVICE = "SECKILL_SERVICE";

    public static final String RESERVE_RESULT_SUCCESS = "SUCCESS";
    public static final String RESERVE_RESULT_DUPLICATE = "DUPLICATE";
    public static final String RESERVE_RESULT_OUT_OF_STOCK = "OUT_OF_STOCK";
    public static final String RESERVE_RESULT_FAILED = "FAILED";
    public static final String ROLLBACK_RESULT_SUCCESS = "SUCCESS";
    public static final String ROLLBACK_RESULT_ALREADY_RELEASED = "ALREADY_RELEASED";
    public static final String ROLLBACK_RESULT_FAILED = "FAILED";

    public static final String ORDER_CREATE_EVENT_TYPE = "ORDER_CREATE_REQUESTED";
    public static final String ORDER_CREATE_EVENT_SOURCE = "SECKILL_SERVICE";
    public static final String ORDER_CREATE_EXCHANGE = "ticket.order.exchange";
    public static final String ORDER_CREATE_ROUTING_KEY = "ticket.order.create";
    public static final String ORDER_CREATE_TASK_STATUS_PENDING = ReliableMessageTaskStatus.PENDING;
    public static final String ORDER_CREATE_TASK_STATUS_RETRYING = ReliableMessageTaskStatus.RETRYING;
    public static final String ORDER_CREATE_TASK_STATUS_SENT = ReliableMessageTaskStatus.SENT;
    public static final String ORDER_CREATE_TASK_STATUS_EXHAUSTED = ReliableMessageTaskStatus.EXHAUSTED;

    /**
     * 禁止实例化常量类。
     */
    private SeckillConstants() {
    }
}
