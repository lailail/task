package com.example.ticket.order.support;

import com.example.ticket.order.service.OrderResultRetryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 下单结果补发调度入口。
 * 用于按固定周期触发结果事件补发扫描，把发送失败但未耗尽的任务重新投递。
 */
@Component
public class OrderResultRetryScheduler {
    private final OrderResultRetryService orderResultRetryService;

    /**
     * 构造下单结果补发调度器。
     *
     * @param orderResultRetryService 下单结果补发服务
     */
    public OrderResultRetryScheduler(OrderResultRetryService orderResultRetryService) {
        this.orderResultRetryService = orderResultRetryService;
    }

    /** 触发一次下单结果补发扫描。 */
    @Scheduled(cron = "${ticket.order.result-retry-cron}")
    public void retryDueTasks() {
        orderResultRetryService.retryDueTasks();
    }
}
