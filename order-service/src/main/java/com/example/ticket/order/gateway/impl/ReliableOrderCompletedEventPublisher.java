package com.example.ticket.order.gateway.impl;

import com.example.ticket.common.event.order.OrderCompletedEvent;
import com.example.ticket.common.reliable.AbstractReliableMessagePublisher;
import com.example.ticket.order.domain.OrderCompleteTaskDO;
import com.example.ticket.order.gateway.OrderCompletedEventPublisher;
import com.example.ticket.order.gateway.OrderCompletedMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 可靠订单完成事件发布器。
 * 用于把订单完成事件接入统一“先落任务、再发送、失败补发”的公共模板。
 */
@Component
public class ReliableOrderCompletedEventPublisher
        extends AbstractReliableMessagePublisher<OrderCompletedEvent, OrderCompleteTaskDO>
        implements OrderCompletedEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ReliableOrderCompletedEventPublisher.class);

    /**
     * 构造可靠订单完成事件发布器。
     *
     * @param taskStore 订单完成任务存储
     * @param orderCompletedMessageSender 底层消息发送器
     * @param objectMapper JSON 工具
     * @param retryIntervalSeconds 首次发送失败后的重试间隔
     * @param maxRetryCount 最大重试次数
     */
    public ReliableOrderCompletedEventPublisher(
            MybatisOrderCompleteTaskStore taskStore,
            OrderCompletedMessageSender orderCompletedMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.order.complete-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.order.complete-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, orderCompletedMessageSender, objectMapper, retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 发布订单完成事件。
     *
     * @param event 订单完成事件
     */
    @Override
    @Transactional
    public void publish(OrderCompletedEvent event) {
        log.debug(
                "收到可靠订单完成事件发布请求，eventId={}, orderId={}, requestId={}, paymentRequestId={}",
                event.getEventId(),
                event.getOrderId(),
                event.getRequestId(),
                event.getPaymentRequestId()
        );
        super.publish(event);
    }

    /**
     * 创建空任务对象。
     *
     * @return 空任务对象
     */
    @Override
    protected OrderCompleteTaskDO createTask() {
        return new OrderCompleteTaskDO();
    }

    /**
     * 提取统一事件键。
     *
     * @param event 订单完成事件
     * @return 统一事件键
     */
    @Override
    protected String extractEventKey(OrderCompletedEvent event) {
        return event.getEventId();
    }

    /**
     * 提取事件类型。
     *
     * @param event 订单完成事件
     * @return 事件类型
     */
    @Override
    protected String extractEventType(OrderCompletedEvent event) {
        return event.getEventType();
    }

    /**
     * 提取业务主键。
     *
     * @param event 订单完成事件
     * @return 业务主键
     */
    @Override
    protected String extractBusinessKey(OrderCompletedEvent event) {
        return String.valueOf(event.getOrderId());
    }
}
