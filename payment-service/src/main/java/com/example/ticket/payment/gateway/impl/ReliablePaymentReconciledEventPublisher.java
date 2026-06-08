package com.example.ticket.payment.gateway.impl;

import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.common.reliable.AbstractReliableMessagePublisher;
import com.example.ticket.payment.domain.PaymentReconciledTaskDO;
import com.example.ticket.payment.gateway.PaymentReconciledEventPublisher;
import com.example.ticket.payment.gateway.PaymentReconciledMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 可靠支付收敛事件发布器。
 * 用于把支付收敛事件接入统一“先落任务、再发消息、失败补发”的模板。
 */
@Component
public class ReliablePaymentReconciledEventPublisher
        extends AbstractReliableMessagePublisher<PaymentReconciledEvent, PaymentReconciledTaskDO>
        implements PaymentReconciledEventPublisher {

    /**
     * 构造可靠支付收敛事件发布器。
     *
     * @param taskStore 支付收敛任务存储
     * @param paymentReconciledMessageSender 底层消息发送器
     * @param objectMapper JSON 工具
     * @param retryIntervalSeconds 重试间隔
     * @param maxRetryCount 最大重试次数
     */
    public ReliablePaymentReconciledEventPublisher(
            MybatisPaymentReconciledTaskStore taskStore,
            PaymentReconciledMessageSender paymentReconciledMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.payment.reconciled-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.payment.reconciled-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, paymentReconciledMessageSender, objectMapper, retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 发布支付收敛事件。
     *
     * @param event 支付收敛事件
     */
    @Override
    @Transactional
    public void publish(PaymentReconciledEvent event) {
        super.publish(event);
    }

    /** 创建空任务。 */
    @Override protected PaymentReconciledTaskDO createTask() { return new PaymentReconciledTaskDO(); }
    /** 提取事件键。 */
    @Override protected String extractEventKey(PaymentReconciledEvent event) { return event.getEventId(); }
    /** 提取事件类型。 */
    @Override protected String extractEventType(PaymentReconciledEvent event) { return event.getEventType(); }
    /** 提取业务主键。 */
    @Override protected String extractBusinessKey(PaymentReconciledEvent event) { return event.getPaymentRequestId(); }
}
