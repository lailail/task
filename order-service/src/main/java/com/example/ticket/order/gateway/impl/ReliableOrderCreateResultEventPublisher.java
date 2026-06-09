package com.example.ticket.order.gateway.impl;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.reliable.AbstractReliableMessagePublisher;
import com.example.ticket.order.domain.OrderResultTaskDO;
import com.example.ticket.order.gateway.OrderCreateResultEventPublisher;
import com.example.ticket.order.gateway.OrderCreateResultMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 可靠下单结果事件发布器。
 * 用于把下单结果事件接到公共可靠消息发布模板上，同时保留订单服务自己的事务边界。
 */
@Component
public class ReliableOrderCreateResultEventPublisher
        extends AbstractReliableMessagePublisher<OrderCreateResultEvent, OrderResultTaskDO>
        implements OrderCreateResultEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ReliableOrderCreateResultEventPublisher.class);

    /**
     * 构造可靠下单结果事件发布器。
     *
     * @param taskStore 下单结果任务存储
     * @param orderCreateResultMessageSender 底层 MQ 发送器
     * @param objectMapper JSON 工具
     * @param retryIntervalSeconds 首次发送失败后的重试间隔
     * @param maxRetryCount 最大重试次数
     */
    public ReliableOrderCreateResultEventPublisher(
            MybatisOrderResultTaskStore taskStore,
            OrderCreateResultMessageSender orderCreateResultMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.order.result-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.order.result-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, orderCreateResultMessageSender, objectMapper, retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 发布下单结果事件。
     * 这里保留原有事务语义，确保补偿任务落库与首次发送编排仍处于同一事务边界。
     *
     * @param event 下单结果事件
     */
    @Override
    @Transactional
    public void publish(OrderCreateResultEvent event) {
        log.debug(
                "收到可靠下单结果事件发布请求，eventId={}, eventType={}, reservationId={}, orderId={}, requestId={}",
                event.getEventId(),
                event.getEventType(),
                event.getReservationId(),
                event.getOrderId(),
                event.getRequestId()
        );
        super.publish(event);
    }

    /**
     * 创建空任务对象。
     *
     * @return 空任务对象
     */
    @Override
    protected OrderResultTaskDO createTask() {
        return new OrderResultTaskDO();
    }

    /**
     * 提取统一事件键。
     *
     * @param event 下单结果事件
     * @return 统一事件键
     */
    @Override
    protected String extractEventKey(OrderCreateResultEvent event) {
        return event.getEventId();
    }

    /**
     * 提取事件类型。
     *
     * @param event 下单结果事件
     * @return 事件类型
     */
    @Override
    protected String extractEventType(OrderCreateResultEvent event) {
        return event.getEventType();
    }

    /**
     * 提取业务主键。
     *
     * @param event 下单结果事件
     * @return 业务主键
     */
    @Override
    protected String extractBusinessKey(OrderCreateResultEvent event) {
        return event.getReservationId();
    }
}
