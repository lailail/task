package com.example.ticket.order.gateway;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.order.domain.OrderResultTaskDO;
import com.example.ticket.order.gateway.impl.ReliableOrderCreateResultEventPublisher;
import com.example.ticket.order.gateway.impl.MybatisOrderResultTaskStore;
import com.example.ticket.order.mapper.OrderResultTaskMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * 可靠下单结果事件发布器单元测试。
 * 用于固定“先登记结果事件补偿事实，再尝试发送；发送失败时保留可补发记录”的发布约束。
 */
@ExtendWith(MockitoExtension.class)
class ReliableOrderCreateResultEventPublisherTest {

    @Mock
    private OrderResultTaskMapper orderResultTaskMapper;

    @Mock
    private OrderCreateResultMessageSender orderCreateResultMessageSender;

    private ReliableOrderCreateResultEventPublisher reliableOrderCreateResultEventPublisher;

    private ObjectMapper objectMapper;

    /**
     * 构造被测可靠发布器。
     * 当前测试通过 mock 持久化层与 MQ 发送层，隔离结果事件补偿编排本身。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        reliableOrderCreateResultEventPublisher = new ReliableOrderCreateResultEventPublisher(
                new MybatisOrderResultTaskStore(orderResultTaskMapper),
                orderCreateResultMessageSender,
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
        OrderCreateResultEvent event = buildResultEvent();

        reliableOrderCreateResultEventPublisher.publish(event);

        ArgumentCaptor<OrderResultTaskDO> insertCaptor = ArgumentCaptor.forClass(OrderResultTaskDO.class);
        ArgumentCaptor<OrderResultTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderResultTaskDO.class);
        verify(orderResultTaskMapper).insert(insertCaptor.capture());
        verify(orderResultTaskMapper).updateById(updateCaptor.capture());

        OrderResultTaskDO insertedTask = insertCaptor.getValue();
        OrderResultTaskDO updatedTask = updateCaptor.getValue();
        assertEquals("result-event-001", insertedTask.getEventKey());
        assertEquals(OrderEventConstants.ORDER_CREATED, insertedTask.getEventType());
        assertEquals(OrderConstants.ORDER_RESULT_TASK_STATUS_PENDING, insertedTask.getTaskStatus());
        assertNotNull(insertedTask.getNextRetryAt());
        assertEquals(OrderConstants.ORDER_RESULT_TASK_STATUS_SENT, updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getLastSentAt());
        OrderCreateResultEvent restoredEvent =
                objectMapper.readValue(insertedTask.getPayloadJson(), OrderCreateResultEvent.class);
        assertEquals("reservation-001", restoredEvent.getReservationId());
    }

    /**
     * 首次发布失败时，应保留待补发记录并记录最近错误，避免结果事件静默丢失。
     */
    @Test
    void should_mark_task_retrying_when_publish_fails() {
        OrderCreateResultEvent event = buildResultEvent();
        doThrow(new IllegalStateException("mq unavailable"))
                .when(orderCreateResultMessageSender)
                .send(event);

        reliableOrderCreateResultEventPublisher.publish(event);

        ArgumentCaptor<OrderResultTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderResultTaskDO.class);
        verify(orderResultTaskMapper).insert(any(OrderResultTaskDO.class));
        verify(orderResultTaskMapper).updateById(updateCaptor.capture());
        assertEquals(OrderConstants.ORDER_RESULT_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(1, updateCaptor.getValue().getRetryCount());
        assertEquals("mq unavailable", updateCaptor.getValue().getLastErrorMessage());
        assertNotNull(updateCaptor.getValue().getNextRetryAt());
    }

    /**
     * 构造下单结果事件。
     *
     * @return 用于测试的下单结果事件
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
