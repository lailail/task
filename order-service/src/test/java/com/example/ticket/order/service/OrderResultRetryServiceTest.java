package com.example.ticket.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.order.domain.OrderResultTaskDO;
import com.example.ticket.order.gateway.OrderCreateResultMessageSender;
import com.example.ticket.order.gateway.impl.MybatisOrderResultTaskStore;
import com.example.ticket.order.mapper.OrderResultTaskMapper;
import com.example.ticket.order.service.impl.OrderResultRetryServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 下单结果补发服务单元测试。
 * 用于固定“只补发到期任务，成功后收敛状态，失败后推进下一次补发时间”的调度边界。
 */
@ExtendWith(MockitoExtension.class)
class OrderResultRetryServiceTest {

    @Mock
    private OrderResultTaskMapper orderResultTaskMapper;

    @Mock
    private OrderCreateResultMessageSender orderCreateResultMessageSender;

    private OrderResultRetryService orderResultRetryService;

    private ObjectMapper objectMapper;

    /**
     * 构造被测补发服务。
     * 当前测试通过 mock 任务表与 MQ 发送器，隔离补发调度决策。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        orderResultRetryService = new OrderResultRetryServiceImpl(
                new MybatisOrderResultTaskStore(orderResultTaskMapper),
                orderCreateResultMessageSender,
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
        OrderResultTaskDO task = buildRetryingTask();
        when(orderResultTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrderResultTaskDO>().setRecords(List.of(task)));

        orderResultRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 19, 30, 0));

        ArgumentCaptor<OrderCreateResultEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreateResultEvent.class);
        ArgumentCaptor<OrderResultTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderResultTaskDO.class);
        verify(orderCreateResultMessageSender).send(eventCaptor.capture());
        verify(orderResultTaskMapper).updateById(updateCaptor.capture());
        assertEquals("result-event-001", eventCaptor.getValue().getEventId());
        assertEquals(OrderConstants.ORDER_RESULT_TASK_STATUS_SENT, updateCaptor.getValue().getTaskStatus());
        assertNotNull(updateCaptor.getValue().getLastSentAt());
    }

    /**
     * 到期补发任务再次发送失败时，应累加重试次数并保留下一次补发时间。
     *
     * @throws JsonProcessingException 当构造任务载荷时抛出异常
     */
    @Test
    void should_reschedule_retry_task_when_retry_fails() throws JsonProcessingException {
        OrderResultTaskDO task = buildRetryingTask();
        when(orderResultTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrderResultTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(orderCreateResultMessageSender)
                .send(any(OrderCreateResultEvent.class));

        orderResultRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 19, 30, 0));

        ArgumentCaptor<OrderResultTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderResultTaskDO.class);
        verify(orderResultTaskMapper).updateById(updateCaptor.capture());
        assertEquals(OrderConstants.ORDER_RESULT_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(3, updateCaptor.getValue().getRetryCount());
        assertEquals("mq timeout", updateCaptor.getValue().getLastErrorMessage());
        assertNotNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 构造待补发任务。
     *
     * @return 待补发任务
     * @throws JsonProcessingException 当序列化事件载荷时抛出异常
     */
    private OrderResultTaskDO buildRetryingTask() throws JsonProcessingException {
        OrderResultTaskDO task = new OrderResultTaskDO();
        task.setTaskId(1L);
        task.setEventKey("result-event-001");
        task.setEventType(OrderEventConstants.ORDER_CREATED);
        task.setBusinessKey("reservation-001");
        task.setPayloadJson(objectMapper.writeValueAsString(buildResultEvent()));
        task.setTaskStatus(OrderConstants.ORDER_RESULT_TASK_STATUS_RETRYING);
        task.setRetryCount(2);
        task.setNextRetryAt(LocalDateTime.of(2026, 6, 5, 19, 0, 0));
        return task;
    }

    /**
     * 构造下单结果事件。
     *
     * @return 用于补发的下单结果事件
     */
    private OrderCreateResultEvent buildResultEvent() {
        OrderCreateResultEvent event = new OrderCreateResultEvent();
        event.setEventId("result-event-001");
        event.setEventType(OrderEventConstants.ORDER_CREATED);
        event.setOccurredAt(Instant.parse("2026-06-05T13:20:00Z"));
        event.setRequestId("req-001");
        event.setIdempotencyKey("idem-001");
        event.setReservationId("reservation-001");
        event.setOrderId(20001L);
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setStatus(OrderConstants.ORDER_STATUS_CREATED);
        event.setSource(OrderEventConstants.SOURCE_ORDER_SERVICE);
        return event;
    }
}
