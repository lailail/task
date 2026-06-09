package com.example.ticket.order.support;

import com.example.ticket.order.service.OrderCompleteRetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单完成事件补发调度器。
 * 用于按固定周期触发订单完成任务的扫描和补发。
 */
@Component
public class OrderCompleteRetryScheduler {
    private static final Logger log = LoggerFactory.getLogger(OrderCompleteRetryScheduler.class);

    private final OrderCompleteRetryService orderCompleteRetryService;

    /**
     * 构造订单完成事件补发调度器。
     *
     * @param orderCompleteRetryService 订单完成事件补发服务
     */
    public OrderCompleteRetryScheduler(OrderCompleteRetryService orderCompleteRetryService) {
        this.orderCompleteRetryService = orderCompleteRetryService;
    }

    /**
     * 按配置周期触发一次订单完成事件补发扫描。
     */
    @Scheduled(cron = "${ticket.order.complete-retry-cron}")
    public void scheduleRetry() {
        log.info("触发订单完成事件补发扫描");
        orderCompleteRetryService.retryDueTasks();
    }
}
