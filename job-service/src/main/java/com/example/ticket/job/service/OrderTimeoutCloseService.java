package com.example.ticket.job.service;

import java.time.LocalDateTime;

/**
 * 超时关单服务。
 * 用于扫描已过期但仍未关闭的订单，并触发统一的库存释放总线。
 */
public interface OrderTimeoutCloseService {

    /**
     * 使用当前时间执行一次超时关单扫描。
     */
    void closeExpiredOrders();

    /**
     * 使用指定时间执行一次超时关单扫描。
     * 该入口主要用于测试或回放场景，避免把时间判断硬编码在业务逻辑里。
     *
     * @param currentTime 当前扫描时间
     */
    void closeExpiredOrders(LocalDateTime currentTime);
}
