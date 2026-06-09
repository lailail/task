package com.example.ticket.payment.support;

import com.example.ticket.payment.service.PaymentProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 支付对账调度器。
 * 用于周期性扫描待收敛支付记录，并触发最小对账回查。
 */
@Component
public class PaymentReconcileScheduler {
    private static final Logger log = LoggerFactory.getLogger(PaymentReconcileScheduler.class);
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final PaymentProcessService paymentProcessService;

    /**
     * 构造支付对账调度器。
     *
     * @param paymentProcessService 支付处理服务
     */
    public PaymentReconcileScheduler(PaymentProcessService paymentProcessService) {
        this.paymentProcessService = paymentProcessService;
    }

    /**
     * 定时执行支付对账回查。
     */
    @Scheduled(cron = "${ticket.payment.reconcile-cron:15/30 * * * * ?}")
    public void reconcile() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE_ID);
        log.info("触发支付对账回查，reconcileTime={}", now);
        paymentProcessService.reconcilePendingPayments(now);
    }
}
