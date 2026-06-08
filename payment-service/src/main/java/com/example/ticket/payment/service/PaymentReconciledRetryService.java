package com.example.ticket.payment.service;

import java.time.LocalDateTime;

/**
 * 支付收敛事件补发服务。
 * 用于扫描到期的支付收敛补偿任务并重新发送消息。
 */
public interface PaymentReconciledRetryService {
    /** 使用当前时间补发到期任务。 */
    void retryDueTasks();
    /**
     * 使用指定时间补发到期任务。
     *
     * @param currentTime 当前时间
     */
    void retryDueTasks(LocalDateTime currentTime);
}
