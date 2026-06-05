package com.example.ticket.job.gateway.impl;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.common.reliable.AbstractReliableMessagePublisher;
import com.example.ticket.job.domain.JobStockReleaseTaskDO;
import com.example.ticket.job.gateway.StockReleaseEventPublisher;
import com.example.ticket.job.gateway.StockReleaseMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 可靠库存释放事件发布器。
 * 用于把库存释放事件接到公共可靠消息发布模板上，同时保留任务服务自己的事务边界。
 */
@Component
public class ReliableStockReleaseEventPublisher
        extends AbstractReliableMessagePublisher<StockReleaseEvent, JobStockReleaseTaskDO>
        implements StockReleaseEventPublisher {

    /**
     * 构造可靠库存释放事件发布器。
     *
     * @param taskStore 库存释放任务存储
     * @param stockReleaseMessageSender 底层 MQ 发送器
     * @param objectMapper JSON 工具
     * @param retryIntervalSeconds 首次发送失败后的重试间隔
     * @param maxRetryCount 最大重试次数
     */
    public ReliableStockReleaseEventPublisher(
            MybatisStockReleaseTaskStore taskStore,
            StockReleaseMessageSender stockReleaseMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.job.stock-release-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.job.stock-release-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, stockReleaseMessageSender, objectMapper, retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 发布库存释放事件。
     * 这里保留原有事务语义，确保补偿任务落库与首次发送编排仍处于同一事务边界。
     *
     * @param event 库存释放事件
     */
    @Override
    @Transactional
    public void publish(StockReleaseEvent event) {
        super.publish(event);
    }

    /**
     * 创建空任务对象。
     *
     * @return 空任务对象
     */
    @Override
    protected JobStockReleaseTaskDO createTask() {
        return new JobStockReleaseTaskDO();
    }

    /**
     * 提取统一事件键。
     *
     * @param event 库存释放事件
     * @return 统一事件键
     */
    @Override
    protected String extractEventKey(StockReleaseEvent event) {
        return event.getEventId();
    }

    /**
     * 提取事件类型。
     *
     * @param event 库存释放事件
     * @return 事件类型
     */
    @Override
    protected String extractEventType(StockReleaseEvent event) {
        return event.getEventType();
    }

    /**
     * 提取业务主键。
     *
     * @param event 库存释放事件
     * @return 业务主键
     */
    @Override
    protected String extractBusinessKey(StockReleaseEvent event) {
        return event.getReservationId();
    }
}
