package com.example.ticket.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentReconciledEvent;
import com.example.ticket.payment.domain.PaymentReconciledTaskDO;
import com.example.ticket.payment.gateway.PaymentReconciledMessageSender;
import com.example.ticket.payment.gateway.impl.MybatisPaymentReconciledTaskStore;
import com.example.ticket.payment.mapper.PaymentReconciledTaskMapper;
import com.example.ticket.payment.service.impl.PaymentReconciledRetryServiceImpl;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付收敛事件补发服务单元测试。
 * 用于固定“只补发到期收敛任务，成功收敛，失败重试或耗尽”的调度边界。
 */
@ExtendWith(MockitoExtension.class)
class PaymentReconciledRetryServiceTest {

    @Mock
    private PaymentReconciledTaskMapper paymentReconciledTaskMapper;

    @Mock
    private PaymentReconciledMessageSender paymentReconciledMessageSender;

    private PaymentReconciledRetryService paymentReconciledRetryService;

    private ObjectMapper objectMapper;

    /**
     * 构造被测补发服务。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        paymentReconciledRetryService = new PaymentReconciledRetryServiceImpl(
                new MybatisPaymentReconciledTaskStore(paymentReconciledTaskMapper),
                paymentReconciledMessageSender,
                objectMapper,
                100,
                30,
                20
        );
    }

    /**
     * 到期补发成功时，应把任务推进到已发送。
     *
     * @throws JsonProcessingException 当构造事件载荷时抛出异常
     */
    @Test
    void should_mark_reconciled_task_sent_when_retry_succeeds() throws JsonProcessingException {
        PaymentReconciledTaskDO task = buildRetryingTask();
        when(paymentReconciledTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<PaymentReconciledTaskDO>().setRecords(List.of(task)));

        paymentReconciledRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        ArgumentCaptor<PaymentReconciledEvent> eventCaptor = ArgumentCaptor.forClass(PaymentReconciledEvent.class);
        ArgumentCaptor<PaymentReconciledTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentReconciledTaskDO.class);
        verify(paymentReconciledMessageSender).send(eventCaptor.capture());
        verify(paymentReconciledTaskMapper).updateById(updateCaptor.capture());
        assertEquals("payment-reconciled-event-001", eventCaptor.getValue().getEventId());
        assertEquals(PaymentConstants.PAYMENT_RECONCILED_TASK_STATUS_SENT, updateCaptor.getValue().getTaskStatus());
        assertNotNull(updateCaptor.getValue().getLastSentAt());
    }

    /**
     * 到期补发失败但未耗尽时，应重新安排下一次补发。
     *
     * @throws JsonProcessingException 当构造事件载荷时抛出异常
     */
    @Test
    void should_reschedule_reconciled_task_when_retry_fails() throws JsonProcessingException {
        PaymentReconciledTaskDO task = buildRetryingTask();
        when(paymentReconciledTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<PaymentReconciledTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(paymentReconciledMessageSender)
                .send(any(PaymentReconciledEvent.class));

        paymentReconciledRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        ArgumentCaptor<PaymentReconciledTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentReconciledTaskDO.class);
        verify(paymentReconciledTaskMapper).updateById(updateCaptor.capture());
        assertEquals(PaymentConstants.PAYMENT_RECONCILED_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(3, updateCaptor.getValue().getRetryCount());
        assertEquals("mq timeout", updateCaptor.getValue().getLastErrorMessage());
        assertNotNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 达到最大重试次数后，应把任务显式收敛为耗尽。
     *
     * @throws JsonProcessingException 当构造事件载荷时抛出异常
     */
    @Test
    void should_mark_reconciled_task_exhausted_when_retry_count_reaches_limit() throws JsonProcessingException {
        PaymentReconciledTaskDO task = buildRetryingTask();
        task.setRetryCount(19);
        task.setMaxRetryCount(20);
        when(paymentReconciledTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<PaymentReconciledTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(paymentReconciledMessageSender)
                .send(any(PaymentReconciledEvent.class));

        paymentReconciledRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        ArgumentCaptor<PaymentReconciledTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentReconciledTaskDO.class);
        verify(paymentReconciledTaskMapper).updateById(updateCaptor.capture());
        assertEquals(PaymentConstants.PAYMENT_RECONCILED_TASK_STATUS_EXHAUSTED, updateCaptor.getValue().getTaskStatus());
        assertEquals(20, updateCaptor.getValue().getRetryCount());
        assertEquals("mq timeout", updateCaptor.getValue().getLastErrorMessage());
        assertNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 构造待补发收敛任务。
     *
     * @return 待补发收敛任务
     * @throws JsonProcessingException 当序列化事件载荷时抛出异常
     */
    private PaymentReconciledTaskDO buildRetryingTask() throws JsonProcessingException {
        PaymentReconciledTaskDO task = new PaymentReconciledTaskDO();
        task.setTaskId(1L);
        task.setEventKey("payment-reconciled-event-001");
        task.setEventType(PaymentEventConstants.PAYMENT_RECONCILED);
        task.setBusinessKey("payment-request-001");
        task.setPayloadJson(objectMapper.writeValueAsString(buildReconciledEvent()));
        task.setTaskStatus(PaymentConstants.PAYMENT_RECONCILED_TASK_STATUS_RETRYING);
        task.setRetryCount(2);
        task.setMaxRetryCount(20);
        task.setNextRetryAt(LocalDateTime.of(2026, 6, 5, 19, 30, 0));
        return task;
    }

    /**
     * 构造用于补发的支付收敛事件。
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
