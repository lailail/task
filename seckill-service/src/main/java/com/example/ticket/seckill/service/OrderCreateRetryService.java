package com.example.ticket.seckill.service;

import java.time.LocalDateTime;

/**
 * 抢票侧下单请求补发服务。
 * 用于扫描首次发送失败或待发送的下单请求任务，并执行补发。
 */
public interface OrderCreateRetryService {

    /**
     * 使用当前时间执行一次补发扫描。
     */
    void retryDueTasks();

    /**
     * 使用指定时间执行一次补发扫描。
     *
     * @param currentTime 当前扫描时间
     */
    void retryDueTasks(LocalDateTime currentTime);
}
