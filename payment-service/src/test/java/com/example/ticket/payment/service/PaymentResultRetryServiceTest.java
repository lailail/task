package com.example.ticket.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.payment.domain.PaymentResultTaskDO;
import com.example.ticket.payment.gateway.PaymentResultMessageSender;
import com.example.ticket.payment.gateway.impl.MybatisPaymentResultTaskStore;
import com.example.ticket.payment.mapper.PaymentResultTaskMapper;
import com.example.ticket.payment.service.impl.PaymentResultRetryServiceImpl;
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
 * 支付结果补发服务单元测试。
 * 用于固定“只补发到期任务，成功后收敛状态，失败后推进下一次补发”的调度边界。
 */
@ExtendWith(MockitoExtension.class)
class PaymentResultRetryServiceTest {

    @Mock
    private PaymentResultTaskMapper paymentResultTaskMapper;

    @Mock
    private PaymentResultMessageSender paymentResultMessageSender;

    private PaymentResultRetryService paymentResultRetryService;

    private ObjectMapper objectMapper;

    /**
     * 构造被测补发服务。
     * 当前测试通过 mock 任务表与消息发送器，隔离补发调度决策本身。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        paymentResultRetryService = new PaymentResultRetryServiceImpl(
                new MybatisPaymentResultTaskStore(paymentResultTaskMapper),
                paymentResultMessageSender,
                objectMapper,
                100,
                30,
                20
        );
    }

    /**
     * 到期补发任务发送成功时，应把状态推进为已发送并回写最后发送时间。
     *
     * @throws JsonProcessingException 当构造任务载荷时抛出异常
     */
    @Test
    void should_mark_retry_task_sent_when_retry_succeeds() throws JsonProcessingException {
        PaymentResultTaskDO task = buildRetryingTask();
        when(paymentResultTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<PaymentResultTaskDO>().setRecords(List.of(task)));

        paymentResultRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        ArgumentCaptor<PaymentResultEvent> eventCaptor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        ArgumentCaptor<PaymentResultTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentResultTaskDO.class);
        verify(paymentResultMessageSender).send(eventCaptor.capture());
        verify(paymentResultTaskMapper).updateById(updateCaptor.capture());
        assertEquals("payment-event-001", eventCaptor.getValue().getEventId());
        assertEquals(PaymentConstants.PAYMENT_RESULT_TASK_STATUS_SENT, updateCaptor.getValue().getTaskStatus());
        assertNotNull(updateCaptor.getValue().getLastSentAt());
    }

    /**
     * 到期补发任务再次发送失败时，应累计重试次数并保留下次补发时间。
     *
     * @throws JsonProcessingException 当构造任务载荷时抛出异常
     */
    @Test
    void should_reschedule_retry_task_when_retry_fails() throws JsonProcessingException {
        PaymentResultTaskDO task = buildRetryingTask();
        when(paymentResultTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<PaymentResultTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(paymentResultMessageSender)
                .send(any(PaymentResultEvent.class));

        paymentResultRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        ArgumentCaptor<PaymentResultTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentResultTaskDO.class);
        verify(paymentResultTaskMapper).updateById(updateCaptor.capture());
        assertEquals(PaymentConstants.PAYMENT_RESULT_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(3, updateCaptor.getValue().getRetryCount());
        assertEquals("mq timeout", updateCaptor.getValue().getLastErrorMessage());
        assertNotNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 到期补发任务达到重试上限后，应收敛为耗尽状态。
     *
     * @throws JsonProcessingException 当构造任务载荷时抛出异常
     */
    @Test
    void should_mark_task_exhausted_when_retry_count_reaches_limit() throws JsonProcessingException {
        PaymentResultTaskDO task = buildRetryingTask();
        task.setRetryCount(19);
        task.setMaxRetryCount(20);
        when(paymentResultTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<PaymentResultTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(paymentResultMessageSender)
                .send(any(PaymentResultEvent.class));

        paymentResultRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        ArgumentCaptor<PaymentResultTaskDO> updateCaptor = ArgumentCaptor.forClass(PaymentResultTaskDO.class);
        verify(paymentResultTaskMapper).updateById(updateCaptor.capture());
        assertEquals(PaymentConstants.PAYMENT_RESULT_TASK_STATUS_EXHAUSTED, updateCaptor.getValue().getTaskStatus());
        assertEquals(20, updateCaptor.getValue().getRetryCount());
        assertEquals("mq timeout", updateCaptor.getValue().getLastErrorMessage());
        assertNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 构造待补发任务。
     *
     * @return 待补发任务
     * @throws JsonProcessingException 当序列化事件载荷时抛出异常
     */
    private PaymentResultTaskDO buildRetryingTask() throws JsonProcessingException {
        PaymentResultTaskDO task = new PaymentResultTaskDO();
        task.setTaskId(1L);
        task.setEventKey("payment-event-001");
        task.setEventType(PaymentEventConstants.PAYMENT_SUCCEEDED);
        task.setBusinessKey("payment-request-001");
        task.setPayloadJson(objectMapper.writeValueAsString(buildPaymentResultEvent()));
        task.setTaskStatus(PaymentConstants.PAYMENT_RESULT_TASK_STATUS_RETRYING);
        task.setRetryCount(2);
        task.setMaxRetryCount(20);
        task.setNextRetryAt(LocalDateTime.of(2026, 6, 5, 19, 30, 0));
        return task;
    }

    /**
     * 构造用于补发的支付结果事件。
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
