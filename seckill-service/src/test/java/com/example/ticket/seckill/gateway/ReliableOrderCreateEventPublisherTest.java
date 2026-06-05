package com.example.ticket.seckill.gateway;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.seckill.domain.OrderCreateTaskDO;
import com.example.ticket.seckill.gateway.impl.MybatisOrderCreateTaskStore;
import com.example.ticket.seckill.gateway.impl.ReliableOrderCreateEventPublisher;
import com.example.ticket.seckill.mapper.OrderCreateTaskMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * 抢票侧可靠下单事件发布器单元测试。
 * 用于固定“先登记补偿任务，再尝试发送；发送失败时保留可补发记录”的发布约束。
 */
@ExtendWith(MockitoExtension.class)
class ReliableOrderCreateEventPublisherTest {

    @Mock
    private OrderCreateTaskMapper orderCreateTaskMapper;

    @Mock
    private OrderCreateMessageSender orderCreateMessageSender;

    private ReliableOrderCreateEventPublisher reliableOrderCreateEventPublisher;

    private ObjectMapper objectMapper;

    /**
     * 构造被测可靠发布器。
     * 当前测试通过 mock 持久化层与 MQ 发送层，隔离抢票侧下单事件补偿编排本身。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        reliableOrderCreateEventPublisher = new ReliableOrderCreateEventPublisher(
                new MybatisOrderCreateTaskStore(orderCreateTaskMapper),
                orderCreateMessageSender,
                objectMapper,
                30,
                20
        );
    }

    /**
     * 首次发布成功时，应先写入补偿记录，再把状态收敛为已发送。
     *
     * @throws JsonProcessingException 当断言载荷序列化结果时抛出异常
     */
    @Test
    void should_create_task_and_mark_sent_when_publish_succeeds() throws JsonProcessingException {
        OrderCreateRequestedEvent event = buildEvent();

        reliableOrderCreateEventPublisher.publish(event);

        ArgumentCaptor<OrderCreateTaskDO> insertCaptor = ArgumentCaptor.forClass(OrderCreateTaskDO.class);
        ArgumentCaptor<OrderCreateTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCreateTaskDO.class);
        verify(orderCreateTaskMapper).insert(insertCaptor.capture());
        verify(orderCreateTaskMapper).updateById(updateCaptor.capture());

        OrderCreateTaskDO insertedTask = insertCaptor.getValue();
        OrderCreateTaskDO updatedTask = updateCaptor.getValue();
        assertEquals("event-001", insertedTask.getEventKey());
        assertEquals(OrderEventConstants.ORDER_CREATE_REQUESTED, insertedTask.getEventType());
        assertEquals(SeckillConstants.ORDER_CREATE_TASK_STATUS_PENDING, insertedTask.getTaskStatus());
        assertNotNull(insertedTask.getNextRetryAt());
        assertEquals(SeckillConstants.ORDER_CREATE_TASK_STATUS_SENT, updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getLastSentAt());

        OrderCreateRequestedEvent restoredEvent =
                objectMapper.readValue(insertedTask.getPayloadJson(), OrderCreateRequestedEvent.class);
        assertEquals("reservation-001", restoredEvent.getReservationId());
    }

    /**
     * 首次发布失败时，应保留待补发记录并记录最近错误。
     */
    @Test
    void should_mark_task_retrying_when_publish_fails() {
        OrderCreateRequestedEvent event = buildEvent();
        doThrow(new IllegalStateException("mq unavailable"))
                .when(orderCreateMessageSender)
                .send(event);

        reliableOrderCreateEventPublisher.publish(event);

        ArgumentCaptor<OrderCreateTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCreateTaskDO.class);
        verify(orderCreateTaskMapper).insert(any(OrderCreateTaskDO.class));
        verify(orderCreateTaskMapper).updateById(updateCaptor.capture());
        assertEquals(SeckillConstants.ORDER_CREATE_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(1, updateCaptor.getValue().getRetryCount());
        assertEquals("mq unavailable", updateCaptor.getValue().getLastErrorMessage());
        assertNotNull(updateCaptor.getValue().getNextRetryAt());
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
