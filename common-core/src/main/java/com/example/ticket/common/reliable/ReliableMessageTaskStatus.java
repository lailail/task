package com.example.ticket.common.reliable;

/**
 * 可靠消息任务状态常量。
 * 用于收敛不同业务域补偿任务的公共状态字面量，避免各模块继续散落重复定义。
 */
public final class ReliableMessageTaskStatus {
    public static final String PENDING = "PENDING";
    public static final String RETRYING = "RETRYING";
    public static final String SENT = "SENT";
    public static final String EXHAUSTED = "EXHAUSTED";

    /**
     * 禁止实例化常量类。
     */
    private ReliableMessageTaskStatus() {
    }
}
