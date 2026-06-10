package com.example.ticket.common.reliable.query;

/**
 * 补偿任务类型常量。
 * 用于统一前后端之间的任务域标识，避免各服务和页面分别硬编码类型字符串。
 */
public final class ReliableMessageTaskTypes {
    public static final String ORDER_CREATE = "ORDER_CREATE";
    public static final String ORDER_RESULT = "ORDER_RESULT";
    public static final String ORDER_COMPLETE = "ORDER_COMPLETE";
    public static final String STOCK_RELEASE = "STOCK_RELEASE";
    public static final String PAYMENT_RESULT = "PAYMENT_RESULT";
    public static final String PAYMENT_RECONCILED = "PAYMENT_RECONCILED";

    /**
     * 禁止实例化常量类。
     */
    private ReliableMessageTaskTypes() {
    }
}
