package com.example.ticket.payment.gateway.impl;

import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.common.reliable.AbstractReliableMessagePublisher;
import com.example.ticket.payment.domain.PaymentResultTaskDO;
import com.example.ticket.payment.gateway.PaymentResultEventPublisher;
import com.example.ticket.payment.gateway.PaymentResultMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 可靠支付结果事件发布器。
 * 用于把支付结果事件接入统一“先落任务、再发消息、失败转补发”的公共模板。
 */
@Component
public class ReliablePaymentResultEventPublisher
        extends AbstractReliableMessagePublisher<PaymentResultEvent, PaymentResultTaskDO>
        implements PaymentResultEventPublisher {

    /**
     * 构造可靠支付结果发布器。
     *
     * @param taskStore 支付结果任务存储
     * @param paymentResultMessageSender 底层支付结果消息发送器
     * @param objectMapper JSON 工具
     * @param retryIntervalSeconds 首次发送失败后的重试间隔
     * @param maxRetryCount 最大重试次数
     */
    public ReliablePaymentResultEventPublisher(
            MybatisPaymentResultTaskStore taskStore,
            PaymentResultMessageSender paymentResultMessageSender,
            ObjectMapper objectMapper,
            @Value("${ticket.payment.result-retry-interval-seconds}") int retryIntervalSeconds,
            @Value("${ticket.payment.result-max-retry-count}") int maxRetryCount
    ) {
        super(taskStore, paymentResultMessageSender, objectMapper, retryIntervalSeconds, maxRetryCount);
    }

    /**
     * 发布支付结果事件。
     * 这里保留支付域自己的事务边界，确保支付事实与补偿任务登记保持同一事务语义。
     *
     * @param event 支付结果事件
     */
    @Override
    @Transactional
    public void publish(PaymentResultEvent event) {
        super.publish(event);
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

    /**
     * 提取统一事件键。
     *
     * @param event 支付结果事件
     * @return 统一事件键
     */
    @Override
    protected String extractEventKey(PaymentResultEvent event) {
        return event.getEventId();
    }

    /**
     * 提取事件类型。
     *
     * @param event 支付结果事件
     * @return 事件类型
     */
    @Override
    protected String extractEventType(PaymentResultEvent event) {
        return event.getEventType();
    }

    /**
     * 提取业务主键。
     *
     * @param event 支付结果事件
     * @return 业务主键
     */
    @Override
    protected String extractBusinessKey(PaymentResultEvent event) {
        return event.getPaymentRequestId();
    }
}
