package com.example.ticket.seckill.support;

import com.example.ticket.seckill.service.OrderCreateRetryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 抢票侧下单请求补发调度入口。
 * 用于按固定周期触发下单请求补发扫描，把发送失败但未耗尽的任务重新投递。
 */
@Component
@ConditionalOnProperty(prefix = "ticket.seckill", name = "order-create-retry-enabled", havingValue = "true", matchIfMissing = true)
public class OrderCreateRetryScheduler {
    private final OrderCreateRetryService orderCreateRetryService;

    /**
     * 构造抢票侧下单请求补发调度器。
     *
     * @param orderCreateRetryService 抢票侧下单请求补发服务
     */
    public OrderCreateRetryScheduler(OrderCreateRetryService orderCreateRetryService) {
        this.orderCreateRetryService = orderCreateRetryService;
    }

    /** 触发一次下单请求补发扫描。 */
    @Scheduled(cron = "${ticket.seckill.order-create-retry-cron}")
    public void retryDueTasks() {
        orderCreateRetryService.retryDueTasks();
    }
}
