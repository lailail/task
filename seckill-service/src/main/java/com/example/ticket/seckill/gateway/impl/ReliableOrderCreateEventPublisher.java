package com.example.ticket.seckill.gateway.impl;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.reliable.AbstractReliableMessagePublisher;
import com.example.ticket.seckill.domain.OrderCreateTaskDO;
import com.example.ticket.seckill.gateway.OrderCreateEventPublisher;
import com.example.ticket.seckill.gateway.OrderCreateMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 抢票侧可靠下单事件发布器。
 * 用于把下单请求事件接到公共可靠消息发布模板上，同时保留抢票服务自己的事务边界。
 */
@Component
public class ReliableOrderCreateEventPublisher
        extends AbstractReliableMessagePublisher<OrderCreateRequestedEvent, OrderCreateTaskDO>
        implements OrderCreateEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ReliableOrderCreateEventPublisher.class);

    /**
     * 构造抢票侧可靠下单事件发布器。
     *
     * @param taskStore 下单请求任务存储
     * @param orderCreateMessageSender 底层 MQ 发送器
     * @param objectMapper JSON 工具
     * @param retryIntervalSeconds 首次发送失败后的重试间隔
     * @param maxRetryCount 最大重试次数
     */
    public ReliableOrderCreateEventPublisher(
            MybatisOrderCreateTaskStore taskStore,
            OrderCreateMessageSender orderCreateMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.seckill.order-create-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.seckill.order-create-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, orderCreateMessageSender, objectMapper, retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 发布下单请求事件。
     * 这里保留当前事务语义，确保补偿任务落库与首次发送编排仍处于同一事务边界。
     *
     * @param event 下单请求事件
     */
    @Override
    @Transactional
    public void publish(OrderCreateRequestedEvent event) {
        log.debug(
                "收到可靠下单请求事件发布请求，eventId={}, reservationId={}, requestId={}, idempotencyKey={}",
                event.getEventId(),
                event.getReservationId(),
                event.getRequestId(),
                event.getIdempotencyKey()
        );
        super.publish(event);
    }

    /**
     * 创建空任务对象。
     *
     * @return 空任务对象
     */
    @Override
    protected OrderCreateTaskDO createTask() {
        return new OrderCreateTaskDO();
    }

    /**
     * 提取统一事件键。
     *
     * @param event 下单请求事件
     * @return 统一事件键
     */
    @Override
    protected String extractEventKey(OrderCreateRequestedEvent event) {
        return event.getEventId();
    }

    /**
     * 提取事件类型。
     *
     * @param event 下单请求事件
     * @return 事件类型
     */
    @Override
    protected String extractEventType(OrderCreateRequestedEvent event) {
        return event.getEventType();
    }

    /**
     * 提取业务主键。
     *
     * @param event 下单请求事件
     * @return 业务主键
     */
    @Override
    protected String extractBusinessKey(OrderCreateRequestedEvent event) {
        return event.getReservationId();
    }
}
