package com.example.ticket.order.service.impl;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.reliable.AbstractReliableMessageRetryService;
import com.example.ticket.order.domain.OrderResultTaskDO;
import com.example.ticket.order.gateway.OrderCreateResultMessageSender;
import com.example.ticket.order.gateway.impl.MybatisOrderResultTaskStore;
import com.example.ticket.order.service.OrderResultRetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 下单结果补发服务实现。
 * 用于把下单结果任务的到期扫描和补发行为适配到公共可靠消息补发模板。
 */
@Service
public class OrderResultRetryServiceImpl
        extends AbstractReliableMessageRetryService<OrderCreateResultEvent, OrderResultTaskDO>
        implements OrderResultRetryService {

    /**
     * 构造下单结果补发服务。
     *
     * @param taskStore 下单结果任务存储
     * @param orderCreateResultMessageSender 底层 MQ 发送器
     * @param objectMapper JSON 工具
     * @param retryBatchSize 单次扫描批量大小
     * @param retryIntervalSeconds 失败后的补发间隔
     * @param maxRetryCount 最大重试次数
     */
    public OrderResultRetryServiceImpl(
            MybatisOrderResultTaskStore taskStore,
            OrderCreateResultMessageSender orderCreateResultMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.order.result-retry-batch-size}") int retryBatchSize,
            @Value("${ticket.order.result-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.order.result-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, orderCreateResultMessageSender, objectMapper, OrderCreateResultEvent.class, retryBatchSize,
                retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 使用当前时间执行一次补发扫描。
     */
    @Override
    public void retryDueTasks() {
        super.retryDueTasks();
    }

    /**
     * 使用指定时间执行一次补发扫描。
     *
     * @param currentTime 当前扫描时间
     */
    @Override
    public void retryDueTasks(LocalDateTime currentTime) {
        super.retryDueTasks(currentTime);
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
}
