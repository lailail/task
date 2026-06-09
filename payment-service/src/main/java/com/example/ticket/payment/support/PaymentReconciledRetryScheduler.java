package com.example.ticket.payment.support;

import com.example.ticket.payment.service.PaymentReconciledRetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付收敛事件补发调度器。
 * 用于周期扫描支付收敛消息的失败任务并重新发送。
 */
@Component
public class PaymentReconciledRetryScheduler {
    private static final Logger log = LoggerFactory.getLogger(PaymentReconciledRetryScheduler.class);

    private final PaymentReconciledRetryService paymentReconciledRetryService;

    /**
     * 构造支付收敛事件补发调度器。
     *
     * @param paymentReconciledRetryService 支付收敛补发服务
     */
    public PaymentReconciledRetryScheduler(PaymentReconciledRetryService paymentReconciledRetryService) {
        this.paymentReconciledRetryService = paymentReconciledRetryService;
    }

    /**
     * 触发一次支付收敛事件补发扫描。
     */
    @Scheduled(cron = "${ticket.payment.reconciled-retry-cron}")
    public void retryDueTasks() {
        log.info("触发支付收敛事件补发扫描");
        paymentReconciledRetryService.retryDueTasks();
    }
}
