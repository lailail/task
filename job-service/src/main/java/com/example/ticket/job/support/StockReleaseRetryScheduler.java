package com.example.ticket.job.support;

import com.example.ticket.job.service.StockReleaseRetryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 库存释放补发调度入口。
 * 用于按固定周期触发释放事件补发扫描，把发送失败但未耗尽的任务重新投递。
 */
@Component
public class StockReleaseRetryScheduler {
    private final StockReleaseRetryService stockReleaseRetryService;

    /**
     * 构造库存释放补发调度器。
     *
     * @param stockReleaseRetryService 库存释放补发服务
     */
    public StockReleaseRetryScheduler(StockReleaseRetryService stockReleaseRetryService) {
        this.stockReleaseRetryService = stockReleaseRetryService;
    }

    /** 触发一次库存释放补发扫描。 */
    @Scheduled(cron = "${ticket.job.stock-release-retry-cron}")
    public void retryDueTasks() {
        stockReleaseRetryService.retryDueTasks();
    }
}
