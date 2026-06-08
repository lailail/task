package com.example.ticket.common.event.payment;

/**
 * 支付事件公共常量。
 * 用于统一支付域与订单域共享的交换机、路由键、事件类型和来源标识，避免跨服务契约漂移。
 */
public final class PaymentEventConstants {
    public static final String PAYMENT_EXCHANGE = "ticket.payment.exchange";
    public static final String PAYMENT_RESULT_QUEUE = "ticket.payment.result.queue";
    public static final String PAYMENT_RESULT_ROUTING_KEY = "ticket.payment.result";
    public static final String PAYMENT_RECONCILED_QUEUE = "ticket.payment.reconciled.queue";
    public static final String PAYMENT_RECONCILED_ROUTING_KEY = "ticket.payment.reconciled";

    public static final String PAYMENT_SUCCEEDED = "PAYMENT_SUCCEEDED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PAYMENT_EXPIRED = "PAYMENT_EXPIRED";
    public static final String PAYMENT_RECONCILED = "PAYMENT_RECONCILED";

    public static final String SOURCE_PAYMENT_SERVICE = "PAYMENT_SERVICE";

    /**
     * 禁止实例化常量类。
     */
    private PaymentEventConstants() {
    }
}
