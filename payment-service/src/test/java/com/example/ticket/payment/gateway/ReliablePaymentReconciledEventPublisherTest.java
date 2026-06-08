package com.example.ticket.payment.gateway;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.payment.domain.PaymentReconciledTaskDO;
import com.example.ticket.payment.gateway.impl.MybatisPaymentReconciledTaskStore;
import com.example.ticket.payment.gateway.impl.ReliablePaymentReconciledEventPublisher;
import com.example.ticket.payment.mapper.PaymentReconciledTaskMapper;
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
 * 可靠支付收敛事件发布器单元测试。
 * 用于固定“先登记补偿任务，再发送收敛事件，失败时留下可补发事实”的约束。
 */
@ExtendWith(MockitoExtension.class)
class ReliablePaymentReconciledEventPublisherTest {

    @Mock
    private PaymentReconciledTaskMapper paymentReconciledTaskMapper;

    @Mock
    private PaymentReconciledMessageSender paymentReconciledMessageSender;

    private ReliablePaymentReconciledEventPublisher reliablePaymentReconciledEventPublisher;

    private ObjectMapper objectMapper;

    /**
     * 构造被测可靠发布器。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        reliablePaymentReconciledEventPublisher = new ReliablePaymentReconciledEventPublisher(
                new MybatisPaymentReconciledTaskStore(paymentReconciledTaskMapper),
                paymentReconciledMessageSender,
                objectMapper,
                30,
                20
        );
    }

    /**
     * 首次发布成功时，应先落补偿任务，再把任务收敛为已发送。
     *
     * @throws JsonProcessingException 当断言载荷反序列化时抛出异常
     */
    @Test
    void should_create_task_and_mark_sent_when_publish_reconciled_event_succeeds() throws JsonProcessingException {
        PaymentReconciledEvent event = buildReconciledEvent();

        reliablePaymentReconciledEventPublisher.publish(event);

        ArgumentCaptor<PaymentReconciledTaskDO> insertCaptor = ArgumentCaptor.forClass(PaymentReconciledTaskDO.class);
        ArgumentCaptor<PaymentReconciledTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentReconciledTaskDO.class);
        verify(paymentReconciledTaskMapper).insert(insertCaptor.capture());
        verify(paymentReconciledTaskMapper).updateById(updateCaptor.capture());

        PaymentReconciledTaskDO insertedTask = insertCaptor.getValue();
        PaymentReconciledTaskDO updatedTask = updateCaptor.getValue();
        assertEquals("payment-reconciled-event-001", insertedTask.getEventKey());
        assertEquals(PaymentEventConstants.PAYMENT_RECONCILED, insertedTask.getEventType());
        assertEquals(PaymentConstants.PAYMENT_RECONCILED_TASK_STATUS_PENDING, insertedTask.getTaskStatus());
        assertEquals("payment-request-001", insertedTask.getBusinessKey());
        assertNotNull(insertedTask.getNextRetryAt());
        assertEquals(PaymentConstants.PAYMENT_RECONCILED_TASK_STATUS_SENT, updatedTask.getTaskStatus());

        PaymentReconciledEvent restoredEvent =
                objectMapper.readValue(insertedTask.getPayloadJson(), PaymentReconciledEvent.class);
        assertEquals("payment-request-001", restoredEvent.getPaymentRequestId());
    }

    /**
     * 首次发布失败时，应把任务推进到待补发状态并记录错误。
     */
    @Test
    void should_mark_task_retrying_when_publish_reconciled_event_fails() {
        PaymentReconciledEvent event = buildReconciledEvent();
        doThrow(new IllegalStateException("mq unavailable"))
                .when(paymentReconciledMessageSender)
                .send(event);

        reliablePaymentReconciledEventPublisher.publish(event);

        ArgumentCaptor<PaymentReconciledTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentReconciledTaskDO.class);
        verify(paymentReconciledTaskMapper).insert(any(PaymentReconciledTaskDO.class));
        verify(paymentReconciledTaskMapper).updateById(updateCaptor.capture());
        assertEquals(PaymentConstants.PAYMENT_RECONCILED_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(1, updateCaptor.getValue().getRetryCount());
        assertEquals("mq unavailable", updateCaptor.getValue().getLastErrorMessage());
    }

    /**
     * 构造支付收敛事件。
     *
     * @return 支付收敛事件
     */
    private PaymentReconciledEvent buildReconciledEvent() {
        PaymentReconciledEvent event = new PaymentReconciledEvent();
        event.setEventId("payment-reconciled-event-001");
        event.setEventType(PaymentEventConstants.PAYMENT_RECONCILED);
        event.setOccurredAt(Instant.parse("2026-06-05T16:00:00Z"));
        event.setRequestId("pay-request-001");
        event.setPaymentRequestId("payment-request-001");
        event.setOrderId(20001L);
        event.setOrderNo("ORD-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setPaymentStatus(PaymentConstants.PAYMENT_STATUS_SUCCESS);
        event.setSource(PaymentConstants.PAYMENT_SOURCE_PAYMENT_SERVICE);
        return event;
    }
}
