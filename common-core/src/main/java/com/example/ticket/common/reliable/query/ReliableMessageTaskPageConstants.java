package com.example.ticket.common.reliable.query;

/**
 * 补偿任务分页查询常量。
 * 用于统一后台治理页的分页默认值与最大页大小，避免各服务散落重复硬编码。
 */
public final class ReliableMessageTaskPageConstants {
    public static final long DEFAULT_PAGE_CURRENT = 1L;
    public static final long DEFAULT_PAGE_SIZE = 10L;
    public static final long MAX_PAGE_SIZE = 100L;

    /**
     * 禁止实例化常量类。
     */
    private ReliableMessageTaskPageConstants() {
    }
}
