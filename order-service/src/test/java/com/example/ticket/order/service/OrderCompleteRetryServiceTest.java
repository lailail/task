package com.example.ticket.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.order.OrderCompletedEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.order.domain.OrderCompleteTaskDO;
import com.example.ticket.order.gateway.OrderCompletedMessageSender;
import com.example.ticket.order.gateway.impl.MybatisOrderCompleteTaskStore;
import com.example.ticket.order.mapper.OrderCompleteTaskMapper;
import com.example.ticket.order.service.impl.OrderCompleteRetryServiceImpl;
import com.example.ticket.order.support.OrderConstants;
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
 * 订单完成事件补发服务单元测试。
 * 用于固定“只补发到期完成任务，成功收敛，失败重试或耗尽”的调度边界。
 */
@ExtendWith(MockitoExtension.class)
class OrderCompleteRetryServiceTest {

    @Mock
    private OrderCompleteTaskMapper orderCompleteTaskMapper;

    @Mock
    private OrderCompletedMessageSender orderCompletedMessageSender;

    private OrderCompleteRetryService orderCompleteRetryService;

    private ObjectMapper objectMapper;

    /**
     * 构造被测补发服务。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        orderCompleteRetryService = new OrderCompleteRetryServiceImpl(
                new MybatisOrderCompleteTaskStore(orderCompleteTaskMapper),
                orderCompletedMessageSender,
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
    void should_mark_complete_task_sent_when_retry_succeeds() throws JsonProcessingException {
        OrderCompleteTaskDO task = buildRetryingTask();
        when(orderCompleteTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrderCompleteTaskDO>().setRecords(List.of(task)));

        orderCompleteRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 30, 0));

        ArgumentCaptor<OrderCompletedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCompletedEvent.class);
        ArgumentCaptor<OrderCompleteTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCompleteTaskDO.class);
        verify(orderCompletedMessageSender).send(eventCaptor.capture());
        verify(orderCompleteTaskMapper).updateById(updateCaptor.capture());
        assertEquals("completed-event-001", eventCaptor.getValue().getEventId());
        assertEquals(OrderConstants.ORDER_COMPLETE_TASK_STATUS_SENT, updateCaptor.getValue().getTaskStatus());
        assertNotNull(updateCaptor.getValue().getLastSentAt());
    }

    /**
     * 到期补发失败且未耗尽时，应重新安排下一次补发。
     *
     * @throws JsonProcessingException 当构造事件载荷时抛出异常
     */
    @Test
    void should_reschedule_complete_task_when_retry_fails() throws JsonProcessingException {
        OrderCompleteTaskDO task = buildRetryingTask();
        when(orderCompleteTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrderCompleteTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(orderCompletedMessageSender)
                .send(any(OrderCompletedEvent.class));

        orderCompleteRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 30, 0));

        ArgumentCaptor<OrderCompleteTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCompleteTaskDO.class);
        verify(orderCompleteTaskMapper).updateById(updateCaptor.capture());
        assertEquals(OrderConstants.ORDER_COMPLETE_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
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
    void should_mark_complete_task_exhausted_when_retry_count_reaches_limit() throws JsonProcessingException {
        OrderCompleteTaskDO task = buildRetryingTask();
        task.setRetryCount(19);
        task.setMaxRetryCount(20);
        when(orderCompleteTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrderCompleteTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(orderCompletedMessageSender)
                .send(any(OrderCompletedEvent.class));

        orderCompleteRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 30, 0));

        ArgumentCaptor<OrderCompleteTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCompleteTaskDO.class);
        verify(orderCompleteTaskMapper).updateById(updateCaptor.capture());
        assertEquals(OrderConstants.ORDER_COMPLETE_TASK_STATUS_EXHAUSTED, updateCaptor.getValue().getTaskStatus());
        assertEquals(20, updateCaptor.getValue().getRetryCount());
        assertEquals("mq timeout", updateCaptor.getValue().getLastErrorMessage());
        assertNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 构造待补发完成任务。
     *
     * @return 待补发完成任务
     * @throws JsonProcessingException 当序列化事件载荷时抛出异常
     */
    private OrderCompleteTaskDO buildRetryingTask() throws JsonProcessingException {
        OrderCompleteTaskDO task = new OrderCompleteTaskDO();
        task.setTaskId(1L);
        task.setEventKey("completed-event-001");
        task.setEventType(OrderEventConstants.ORDER_COMPLETED);
        task.setBusinessKey("20001");
        task.setPayloadJson(objectMapper.writeValueAsString(buildCompletedEvent()));
        task.setTaskStatus(OrderConstants.ORDER_COMPLETE_TASK_STATUS_RETRYING);
        task.setRetryCount(2);
        task.setMaxRetryCount(20);
        task.setNextRetryAt(LocalDateTime.of(2026, 6, 5, 20, 0, 0));
        return task;
    }

    /**
     * 构造用于补发的订单完成事件。
     *
     * @return 订单完成事件
     */
    private OrderCompletedEvent buildCompletedEvent() {
        OrderCompletedEvent event = new OrderCompletedEvent();
        event.setEventId("completed-event-001");
        event.setEventType(OrderEventConstants.ORDER_COMPLETED);
        event.setOccurredAt(Instant.parse("2026-06-05T16:30:00Z"));
        event.setRequestId("req-001");
        event.setOrderId(20001L);
        event.setOrderNo("ORD-001");
        event.setPaymentRequestId("payment-request-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setStatus(OrderConstants.ORDER_STATUS_COMPLETED);
        event.setSource(OrderConstants.ORDER_SOURCE_ORDER_SERVICE);
        return event;
    }
}
