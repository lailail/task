package com.example.ticket.job.service;

import java.time.LocalDateTime;

/**
 * 库存释放补发服务。
 * 用于扫描到期的补偿任务并重新发送释放事件，兜住 MQ 发送阶段的失败场景。
 */
public interface StockReleaseRetryService {

    /** 使用当前时间执行一次补发扫描。 */
    void retryDueTasks();

    /**
     * 使用指定时间执行一次补发扫描。
     *
     * @param currentTime 当前扫描时间
     */
    void retryDueTasks(LocalDateTime currentTime);
}
