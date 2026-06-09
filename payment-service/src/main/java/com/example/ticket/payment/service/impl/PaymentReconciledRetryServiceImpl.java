package com.example.ticket.payment.service.impl;

import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.common.reliable.AbstractReliableMessageRetryService;
import com.example.ticket.payment.domain.PaymentReconciledTaskDO;
import com.example.ticket.payment.gateway.PaymentReconciledMessageSender;
import com.example.ticket.payment.gateway.impl.MybatisPaymentReconciledTaskStore;
import com.example.ticket.payment.service.PaymentReconciledRetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 支付收敛事件补发服务实现。
 * 用于把支付收敛任务的扫描和重发适配到公共可靠消息补发模板。
 */
@Service
public class PaymentReconciledRetryServiceImpl
        extends AbstractReliableMessageRetryService<PaymentReconciledEvent, PaymentReconciledTaskDO>
        implements PaymentReconciledRetryService {
    private static final Logger log = LoggerFactory.getLogger(PaymentReconciledRetryServiceImpl.class);

    /**
     * 构造支付收敛事件补发服务。
     *
     * @param taskStore 支付收敛任务存储
     * @param paymentReconciledMessageSender 底层消息发送器
     * @param objectMapper JSON 工具
     * @param retryBatchSize 扫描批量大小
     * @param retryIntervalSeconds 补发间隔
     * @param maxRetryCount 最大重试次数
     */
    public PaymentReconciledRetryServiceImpl(
            MybatisPaymentReconciledTaskStore taskStore,
            PaymentReconciledMessageSender paymentReconciledMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.payment.reconciled-retry-batch-size}") int retryBatchSize,
            @Value("${ticket.payment.reconciled-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.payment.reconciled-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, paymentReconciledMessageSender, objectMapper, PaymentReconciledEvent.class, retryBatchSize,
                retryIntervalSeconds, maxRetryCount);
    }

    /** 使用当前时间补发。 */
    @Override public void retryDueTasks() {
        log.debug("支付收敛事件补发服务开始执行默认时间补发扫描");
        super.retryDueTasks();
    }
    /** 使用指定时间补发。 */
    @Override public void retryDueTasks(LocalDateTime currentTime) {
        log.debug("支付收敛事件补发服务开始执行指定时间补发扫描，scanTime={}", currentTime);
        super.retryDueTasks(currentTime);
    }
    /** 创建空任务。 */
    @Override protected PaymentReconciledTaskDO createTask() { return new PaymentReconciledTaskDO(); }
}
