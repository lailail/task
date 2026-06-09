package com.example.ticket.order.service.impl;

import com.example.ticket.common.event.order.OrderCompletedEvent;
import com.example.ticket.common.reliable.AbstractReliableMessageRetryService;
import com.example.ticket.order.domain.OrderCompleteTaskDO;
import com.example.ticket.order.gateway.OrderCompletedMessageSender;
import com.example.ticket.order.gateway.impl.MybatisOrderCompleteTaskStore;
import com.example.ticket.order.service.OrderCompleteRetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 订单完成事件补发服务实现。
 * 用于把订单完成任务的扫描和重发行为适配到公共可靠消息补发模板。
 */
@Service
public class OrderCompleteRetryServiceImpl
        extends AbstractReliableMessageRetryService<OrderCompletedEvent, OrderCompleteTaskDO>
        implements OrderCompleteRetryService {
    private static final Logger log = LoggerFactory.getLogger(OrderCompleteRetryServiceImpl.class);

    /**
     * 构造订单完成事件补发服务。
     *
     * @param taskStore 订单完成任务存储
     * @param orderCompletedMessageSender 底层消息发送器
     * @param objectMapper JSON 工具
     * @param retryBatchSize 单次扫描批量大小
     * @param retryIntervalSeconds 补发间隔
     * @param maxRetryCount 最大重试次数
     */
    public OrderCompleteRetryServiceImpl(
            MybatisOrderCompleteTaskStore taskStore,
            OrderCompletedMessageSender orderCompletedMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.order.complete-retry-batch-size}") int retryBatchSize,
            @Value("${ticket.order.complete-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.order.complete-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, orderCompletedMessageSender, objectMapper, OrderCompletedEvent.class, retryBatchSize,
                retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 使用当前时间执行一次补发扫描。
     */
    @Override
    public void retryDueTasks() {
        log.debug("订单完成事件补发服务开始执行默认时间补发扫描");
        super.retryDueTasks();
    }

    /**
     * 使用指定时间执行一次补发扫描。
     *
     * @param currentTime 当前扫描时间
     */
    @Override
    public void retryDueTasks(LocalDateTime currentTime) {
        log.debug("订单完成事件补发服务开始执行指定时间补发扫描，scanTime={}", currentTime);
        super.retryDueTasks(currentTime);
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
}
