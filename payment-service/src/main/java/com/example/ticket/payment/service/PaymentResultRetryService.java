package com.example.ticket.payment.service;

import java.time.LocalDateTime;

/**
 * 支付结果补发服务。
 * 用于扫描到期的支付结果补偿任务，并重新投递支付结果事件。
 */
public interface PaymentResultRetryService {

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
