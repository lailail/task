package com.example.ticket.job.service.impl;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.common.reliable.AbstractReliableMessageRetryService;
import com.example.ticket.job.domain.JobStockReleaseTaskDO;
import com.example.ticket.job.gateway.StockReleaseMessageSender;
import com.example.ticket.job.gateway.impl.MybatisStockReleaseTaskStore;
import com.example.ticket.job.service.StockReleaseRetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 库存释放补发服务实现。
 * 用于把库存释放任务的到期扫描和补发行为适配到公共可靠消息补发模板。
 */
@Service
public class StockReleaseRetryServiceImpl
        extends AbstractReliableMessageRetryService<StockReleaseEvent, JobStockReleaseTaskDO>
        implements StockReleaseRetryService {
    private static final Logger log = LoggerFactory.getLogger(StockReleaseRetryServiceImpl.class);

    /**
     * 构造库存释放补发服务。
     *
     * @param taskStore 库存释放任务存储
     * @param stockReleaseMessageSender 底层 MQ 发送器
     * @param objectMapper JSON 工具
     * @param retryBatchSize 单次扫描批量大小
     * @param retryIntervalSeconds 失败后的补发间隔
     * @param maxRetryCount 最大重试次数
     */
    public StockReleaseRetryServiceImpl(
            MybatisStockReleaseTaskStore taskStore,
            StockReleaseMessageSender stockReleaseMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.job.stock-release-retry-batch-size}") int retryBatchSize,
            @Value("${ticket.job.stock-release-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.job.stock-release-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, stockReleaseMessageSender, objectMapper, StockReleaseEvent.class, retryBatchSize,
                retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 使用当前时间执行一次补发扫描。
     */
    @Override
    public void retryDueTasks() {
        log.debug("库存释放补发服务开始执行默认时间补发扫描");
        super.retryDueTasks();
    }

    /**
     * 使用指定时间执行一次补发扫描。
     *
     * @param currentTime 当前扫描时间
     */
    @Override
    public void retryDueTasks(LocalDateTime currentTime) {
        log.debug("库存释放补发服务开始执行指定时间补发扫描，scanTime={}", currentTime);
        super.retryDueTasks(currentTime);
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
}
