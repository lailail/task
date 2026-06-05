package com.example.ticket.common.event.stock;

/**
 * 库存事件公共常量。
 * 用于集中维护库存释放链路共享的交换机、队列、路由键和事件类型，避免跨服务消息契约硬编码散落。
 */
public final class StockEventConstants {
    public static final String STOCK_RELEASE_EXCHANGE = "ticket.stock.exchange";
    public static final String STOCK_RELEASE_QUEUE = "ticket.stock.release.queue";
    public static final String STOCK_RELEASE_ROUTING_KEY = "ticket.stock.release";

    public static final String ORDER_TIMEOUT_RELEASE = "ORDER_TIMEOUT_RELEASE";
    public static final String ORDER_CANCEL_RELEASE = "ORDER_CANCEL_RELEASE";
    public static final String ORDER_CREATE_FAILED_RELEASE = "ORDER_CREATE_FAILED_RELEASE";

    /**
     * 禁止实例化常量类。
     */
    private StockEventConstants() {
    }
}
