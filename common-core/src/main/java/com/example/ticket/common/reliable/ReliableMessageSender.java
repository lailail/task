package com.example.ticket.common.reliable;

/**
 * 可靠消息底层发送器抽象。
 * 用于把模板层与具体 MQ 客户端解耦，避免公共模板直接依赖业务模块的发送实现。
 *
 * @param <TEvent> 事件类型
 */
public interface ReliableMessageSender<TEvent> {
    /**
     * 发送事件到消息总线。
     *
     * @param event 待发送事件
     */
    void send(TEvent event);
}
