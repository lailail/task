package com.example.ticket.seckill.service.impl;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.reliable.AbstractReliableMessageRetryService;
import com.example.ticket.seckill.domain.OrderCreateTaskDO;
import com.example.ticket.seckill.gateway.OrderCreateMessageSender;
import com.example.ticket.seckill.gateway.impl.MybatisOrderCreateTaskStore;
import com.example.ticket.seckill.service.OrderCreateRetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 抢票侧下单请求补发服务实现。
 * 用于把下单请求任务的到期扫描和补发行为适配到公共可靠消息补发模板。
 */
@Service
public class OrderCreateRetryServiceImpl
        extends AbstractReliableMessageRetryService<OrderCreateRequestedEvent, OrderCreateTaskDO>
        implements OrderCreateRetryService {

    /**
     * 构造抢票侧下单请求补发服务。
     *
     * @param taskStore 下单请求任务存储
     * @param orderCreateMessageSender 底层 MQ 发送器
     * @param objectMapper JSON 工具
     * @param retryBatchSize 单次扫描批量大小
     * @param retryIntervalSeconds 失败后的补发间隔
     * @param maxRetryCount 最大重试次数
     */
    public OrderCreateRetryServiceImpl(
            MybatisOrderCreateTaskStore taskStore,
            OrderCreateMessageSender orderCreateMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.seckill.order-create-retry-batch-size}") int retryBatchSize,
            @Value("${ticket.seckill.order-create-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.seckill.order-create-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, orderCreateMessageSender, objectMapper, OrderCreateRequestedEvent.class, retryBatchSize,
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
    protected OrderCreateTaskDO createTask() {
        return new OrderCreateTaskDO();
    }
}
