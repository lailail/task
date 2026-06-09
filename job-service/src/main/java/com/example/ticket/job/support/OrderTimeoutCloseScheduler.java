package com.example.ticket.job.support;

import com.example.ticket.job.service.OrderTimeoutCloseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时关单调度器。
 * 用于以定时任务的方式触发超时关单服务，本身不承载额外业务规则。
 */
@Component
public class OrderTimeoutCloseScheduler {
    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutCloseScheduler.class);

    private final OrderTimeoutCloseService orderTimeoutCloseService;

    /**
     * 构造超时关单调度器。
     *
     * @param orderTimeoutCloseService 超时关单服务
     */
    public OrderTimeoutCloseScheduler(OrderTimeoutCloseService orderTimeoutCloseService) {
        this.orderTimeoutCloseService = orderTimeoutCloseService;
    }

    /**
     * 按配置周期执行一次超时关单扫描。
     */
    @Scheduled(cron = "${ticket.job.order-timeout-close-cron}")
    public void run() {
        runOnce();
    }

    /**
     * 执行一次超时关单扫描。
     * 提供独立方法是为了让测试可以直接验证委托关系，而不依赖真实调度器。
     */
    public void runOnce() {
        log.info("触发超时关单扫描");
        orderTimeoutCloseService.closeExpiredOrders();
    }
}
