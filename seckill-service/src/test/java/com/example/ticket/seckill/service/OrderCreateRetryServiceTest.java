package com.example.ticket.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.seckill.domain.OrderCreateTaskDO;
import com.example.ticket.seckill.gateway.OrderCreateMessageSender;
import com.example.ticket.seckill.gateway.impl.MybatisOrderCreateTaskStore;
import com.example.ticket.seckill.mapper.OrderCreateTaskMapper;
import com.example.ticket.seckill.service.impl.OrderCreateRetryServiceImpl;
import com.example.ticket.seckill.support.SeckillConstants;
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
 * 抢票侧下单事件补发服务单元测试。
 * 用于固定“只补发到期任务，成功后收敛状态，失败后推进下一次补发时间”的调度边界。
 */
@ExtendWith(MockitoExtension.class)
class OrderCreateRetryServiceTest {

    @Mock
    private OrderCreateTaskMapper orderCreateTaskMapper;

    @Mock
    private OrderCreateMessageSender orderCreateMessageSender;

    private OrderCreateRetryService orderCreateRetryService;

    private ObjectMapper objectMapper;

    /**
     * 构造被测补发服务。
     * 当前测试通过 mock 任务表与 MQ 发送器，隔离补发调度决策。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        orderCreateRetryService = new OrderCreateRetryServiceImpl(
                new MybatisOrderCreateTaskStore(orderCreateTaskMapper),
                orderCreateMessageSender,
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
        OrderCreateTaskDO task = buildRetryingTask();
        when(orderCreateTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrderCreateTaskDO>().setRecords(List.of(task)));

        orderCreateRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        ArgumentCaptor<OrderCreateRequestedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreateRequestedEvent.class);
        ArgumentCaptor<OrderCreateTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCreateTaskDO.class);
        verify(orderCreateMessageSender).send(eventCaptor.capture());
        verify(orderCreateTaskMapper).updateById(updateCaptor.capture());
        assertEquals("event-001", eventCaptor.getValue().getEventId());
        assertEquals(SeckillConstants.ORDER_CREATE_TASK_STATUS_SENT, updateCaptor.getValue().getTaskStatus());
        assertNotNull(updateCaptor.getValue().getLastSentAt());
    }

    /**
     * 到期补发任务再次发送失败时，应累加重试次数并保留下一次补发时间。
     *
     * @throws JsonProcessingException 当构造任务载荷时抛出异常
     */
    @Test
    void should_reschedule_retry_task_when_retry_fails() throws JsonProcessingException {
        OrderCreateTaskDO task = buildRetryingTask();
        when(orderCreateTaskMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrderCreateTaskDO>().setRecords(List.of(task)));
        doThrow(new IllegalStateException("mq timeout"))
                .when(orderCreateMessageSender)
                .send(any(OrderCreateRequestedEvent.class));

        orderCreateRetryService.retryDueTasks(LocalDateTime.of(2026, 6, 5, 20, 0, 0));

        ArgumentCaptor<OrderCreateTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCreateTaskDO.class);
        verify(orderCreateTaskMapper).updateById(updateCaptor.capture());
        assertEquals(SeckillConstants.ORDER_CREATE_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
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
    private OrderCreateTaskDO buildRetryingTask() throws JsonProcessingException {
        OrderCreateTaskDO task = new OrderCreateTaskDO();
        task.setTaskId(1L);
        task.setEventKey("event-001");
        task.setEventType(OrderEventConstants.ORDER_CREATE_REQUESTED);
        task.setBusinessKey("reservation-001");
        task.setPayloadJson(objectMapper.writeValueAsString(buildEvent()));
        task.setTaskStatus(SeckillConstants.ORDER_CREATE_TASK_STATUS_RETRYING);
        task.setRetryCount(2);
        task.setNextRetryAt(LocalDateTime.of(2026, 6, 5, 19, 30, 0));
        return task;
    }

    /**
     * 构造标准下单事件。
     *
     * @return 下单事件
     */
    private OrderCreateRequestedEvent buildEvent() {
        OrderCreateRequestedEvent event = new OrderCreateRequestedEvent();
        event.setEventId("event-001");
        event.setEventType(OrderEventConstants.ORDER_CREATE_REQUESTED);
        event.setOccurredAt(Instant.parse("2026-06-05T11:00:00Z"));
        event.setRequestId("req-001");
        event.setIdempotencyKey("idem-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setExpireAt(Instant.parse("2026-06-05T11:15:00Z"));
        event.setSource(OrderEventConstants.SOURCE_SECKILL_SERVICE);
        return event;
    }
}
