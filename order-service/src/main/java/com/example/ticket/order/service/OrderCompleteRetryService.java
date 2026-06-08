package com.example.ticket.order.service;

import java.time.LocalDateTime;

/**
 * 订单完成事件补发服务。
 * 用于扫描并补发发送失败的订单完成事件任务。
 */
public interface OrderCompleteRetryService {

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
