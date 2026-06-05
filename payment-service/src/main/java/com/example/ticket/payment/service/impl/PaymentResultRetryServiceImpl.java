package com.example.ticket.payment.service.impl;

import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.common.reliable.AbstractReliableMessageRetryService;
import com.example.ticket.payment.domain.PaymentResultTaskDO;
import com.example.ticket.payment.gateway.PaymentResultMessageSender;
import com.example.ticket.payment.gateway.impl.MybatisPaymentResultTaskStore;
import com.example.ticket.payment.service.PaymentResultRetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 支付结果补发服务实现。
 * 用于把支付域到期补偿任务的扫描与重发行为适配到公共可靠消息补发模板。
 */
@Service
public class PaymentResultRetryServiceImpl
        extends AbstractReliableMessageRetryService<PaymentResultEvent, PaymentResultTaskDO>
        implements PaymentResultRetryService {

    /**
     * 构造支付结果补发服务。
     *
     * @param taskStore 支付结果任务存储
     * @param paymentResultMessageSender 底层消息发送器
     * @param objectMapper JSON 工具
     * @param retryBatchSize 单次扫描批量大小
     * @param retryIntervalSeconds 补发失败后的下一次重试间隔
     * @param maxRetryCount 最大重试次数
     */
    public PaymentResultRetryServiceImpl(
            MybatisPaymentResultTaskStore taskStore,
            PaymentResultMessageSender paymentResultMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.payment.result-retry-batch-size}") int retryBatchSize,
            @Value("${ticket.payment.result-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.payment.result-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, paymentResultMessageSender, objectMapper, PaymentResultEvent.class, retryBatchSize,
                retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 使用当前时间执行一次补发扫描。
     */
    @Override
    public void retryDueTasks() {
        super.retryDueTasks();
    }

    /**
     * 使用指定时间执行一次补发扫描。
     *
     * @param currentTime 当前扫描时间
     */
    @Override
    public void retryDueTasks(LocalDateTime currentTime) {
        super.retryDueTasks(currentTime);
    }

    /**
     * 创建空任务对象。
     *
     * @return 空任务对象
     */
    @Override
    protected PaymentResultTaskDO createTask() {
        return new PaymentResultTaskDO();
    }
}
