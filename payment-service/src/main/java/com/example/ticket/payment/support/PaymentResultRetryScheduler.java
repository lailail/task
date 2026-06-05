package com.example.ticket.payment.support;

import com.example.ticket.payment.service.PaymentResultRetryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付结果补发调度入口。
 * 用于周期性扫描发送失败但尚未耗尽的支付结果任务，并重新投递消息。
 */
@Component
public class PaymentResultRetryScheduler {
    private final PaymentResultRetryService paymentResultRetryService;

    /**
     * 构造支付结果补发调度器。
     *
     * @param paymentResultRetryService 支付结果补发服务
     */
    public PaymentResultRetryScheduler(PaymentResultRetryService paymentResultRetryService) {
        this.paymentResultRetryService = paymentResultRetryService;
    }

    /**
     * 触发一次支付结果补发扫描。
     */
    @Scheduled(cron = "${ticket.payment.result-retry-cron}")
    public void retryDueTasks() {
        paymentResultRetryService.retryDueTasks();
    }
}
