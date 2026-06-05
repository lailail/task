package com.example.ticket.payment.gateway;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.payment.domain.PaymentResultTaskDO;
import com.example.ticket.payment.gateway.impl.MybatisPaymentResultTaskStore;
import com.example.ticket.payment.gateway.impl.ReliablePaymentResultEventPublisher;
import com.example.ticket.payment.mapper.PaymentResultTaskMapper;
import com.example.ticket.payment.support.PaymentConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * 可靠支付结果事件发布器单元测试。
 * 用于固定“先登记补偿任务、再尝试发送；失败时留下可补发记录”的发布约束。
 */
@ExtendWith(MockitoExtension.class)
class ReliablePaymentResultEventPublisherTest {

    @Mock
    private PaymentResultTaskMapper paymentResultTaskMapper;

    @Mock
    private PaymentResultMessageSender paymentResultMessageSender;

    private ReliablePaymentResultEventPublisher reliablePaymentResultEventPublisher;

    private ObjectMapper objectMapper;

    /**
     * 构造被测可靠发布器。
     * 当前测试通过 mock 持久化层与 MQ 发送层，隔离支付结果可靠发布编排本身。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        reliablePaymentResultEventPublisher = new ReliablePaymentResultEventPublisher(
                new MybatisPaymentResultTaskStore(paymentResultTaskMapper),
                paymentResultMessageSender,
                objectMapper,
                30,
                20
        );
    }

    /**
     * 首次发布成功时，应先写入补偿记录，再把状态收敛为已发送。
     *
     * @throws JsonProcessingException 当断言载荷反序列化时抛出异常
     */
    @Test
    void should_create_task_and_mark_sent_when_publish_succeeds() throws JsonProcessingException {
        PaymentResultEvent event = buildPaymentResultEvent();

        reliablePaymentResultEventPublisher.publish(event);

        ArgumentCaptor<PaymentResultTaskDO> insertCaptor = ArgumentCaptor.forClass(PaymentResultTaskDO.class);
        ArgumentCaptor<PaymentResultTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentResultTaskDO.class);
        verify(paymentResultTaskMapper).insert(insertCaptor.capture());
        verify(paymentResultTaskMapper).updateById(updateCaptor.capture());

        PaymentResultTaskDO insertedTask = insertCaptor.getValue();
        PaymentResultTaskDO updatedTask = updateCaptor.getValue();
        assertEquals("payment-event-001", insertedTask.getEventKey());
        assertEquals(PaymentEventConstants.PAYMENT_SUCCEEDED, insertedTask.getEventType());
        assertEquals(PaymentConstants.PAYMENT_RESULT_TASK_STATUS_PENDING, insertedTask.getTaskStatus());
        assertEquals("payment-request-001", insertedTask.getBusinessKey());
        assertNotNull(insertedTask.getNextRetryAt());
        assertEquals(PaymentConstants.PAYMENT_RESULT_TASK_STATUS_SENT, updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getLastSentAt());

        PaymentResultEvent restoredEvent =
                objectMapper.readValue(insertedTask.getPayloadJson(), PaymentResultEvent.class);
        assertEquals("payment-request-001", restoredEvent.getPaymentRequestId());
    }

    /**
     * 首次发布失败时，应把任务推进到待补发状态并记录错误信息。
     */
    @Test
    void should_mark_task_retrying_when_publish_fails() {
        PaymentResultEvent event = buildPaymentResultEvent();
        doThrow(new IllegalStateException("mq unavailable"))
                .when(paymentResultMessageSender)
                .send(event);

        reliablePaymentResultEventPublisher.publish(event);

        ArgumentCaptor<PaymentResultTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentResultTaskDO.class);
        verify(paymentResultTaskMapper).insert(any(PaymentResultTaskDO.class));
        verify(paymentResultTaskMapper).updateById(updateCaptor.capture());
        assertEquals(PaymentConstants.PAYMENT_RESULT_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(1, updateCaptor.getValue().getRetryCount());
        assertEquals("mq unavailable", updateCaptor.getValue().getLastErrorMessage());
        assertNotNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 构造测试用支付结果事件。
     *
     * @return 支付结果事件
     */
    private PaymentResultEvent buildPaymentResultEvent() {
        PaymentResultEvent event = new PaymentResultEvent();
        event.setEventId("payment-event-001");
        event.setEventType(PaymentEventConstants.PAYMENT_SUCCEEDED);
        event.setOccurredAt(Instant.parse("2026-06-05T15:00:00Z"));
        event.setRequestId("pay-request-001");
        event.setPaymentRequestId("payment-request-001");
        event.setOrderId(20001L);
        event.setOrderNo("ORD-001");
        event.setIdempotencyKey("payment-request-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setReason("MOCK_NOTIFY");
        event.setSource(PaymentEventConstants.SOURCE_PAYMENT_SERVICE);
        return event;
    }
}
