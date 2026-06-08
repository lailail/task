package com.example.ticket.order.gateway;

import com.example.ticket.common.event.order.OrderCompletedEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.order.domain.OrderCompleteTaskDO;
import com.example.ticket.order.gateway.impl.MybatisOrderCompleteTaskStore;
import com.example.ticket.order.gateway.impl.ReliableOrderCompletedEventPublisher;
import com.example.ticket.order.mapper.OrderCompleteTaskMapper;
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
 * 可靠订单完成事件发布器单元测试。
 * 用于固定“先登记完成事件补偿任务，再发送 MQ，失败时留下可补发事实”的约束。
 */
@ExtendWith(MockitoExtension.class)
class ReliableOrderCompletedEventPublisherTest {

    @Mock
    private OrderCompleteTaskMapper orderCompleteTaskMapper;

    @Mock
    private OrderCompletedMessageSender orderCompletedMessageSender;

    private ReliableOrderCompletedEventPublisher reliableOrderCompletedEventPublisher;

    private ObjectMapper objectMapper;

    /**
     * 构造被测可靠发布器。
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        reliableOrderCompletedEventPublisher = new ReliableOrderCompletedEventPublisher(
                new MybatisOrderCompleteTaskStore(orderCompleteTaskMapper),
                orderCompletedMessageSender,
                objectMapper,
                30,
                20
        );
    }

    /**
     * 首次发送成功时，应先写入任务，再把任务收敛为已发送。
     *
     * @throws JsonProcessingException 当断言载荷反序列化时抛出异常
     */
    @Test
    void should_create_task_and_mark_sent_when_publish_completed_event_succeeds() throws JsonProcessingException {
        OrderCompletedEvent event = buildCompletedEvent();

        reliableOrderCompletedEventPublisher.publish(event);

        ArgumentCaptor<OrderCompleteTaskDO> insertCaptor = ArgumentCaptor.forClass(OrderCompleteTaskDO.class);
        ArgumentCaptor<OrderCompleteTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCompleteTaskDO.class);
        verify(orderCompleteTaskMapper).insert(insertCaptor.capture());
        verify(orderCompleteTaskMapper).updateById(updateCaptor.capture());

        OrderCompleteTaskDO insertedTask = insertCaptor.getValue();
        OrderCompleteTaskDO updatedTask = updateCaptor.getValue();
        assertEquals("completed-event-001", insertedTask.getEventKey());
        assertEquals(OrderEventConstants.ORDER_COMPLETED, insertedTask.getEventType());
        assertEquals(OrderConstants.ORDER_COMPLETE_TASK_STATUS_PENDING, insertedTask.getTaskStatus());
        assertEquals("20001", insertedTask.getBusinessKey());
        assertNotNull(insertedTask.getNextRetryAt());
        assertEquals(OrderConstants.ORDER_COMPLETE_TASK_STATUS_SENT, updatedTask.getTaskStatus());

        OrderCompletedEvent restoredEvent =
                objectMapper.readValue(insertedTask.getPayloadJson(), OrderCompletedEvent.class);
        assertEquals(20001L, restoredEvent.getOrderId());
    }

    /**
     * 首次发送失败时，应保留待补发任务并记录错误。
     */
    @Test
    void should_mark_task_retrying_when_publish_completed_event_fails() {
        OrderCompletedEvent event = buildCompletedEvent();
        doThrow(new IllegalStateException("mq unavailable"))
                .when(orderCompletedMessageSender)
                .send(event);

        reliableOrderCompletedEventPublisher.publish(event);

        ArgumentCaptor<OrderCompleteTaskDO> updateCaptor = ArgumentCaptor.forClass(OrderCompleteTaskDO.class);
        verify(orderCompleteTaskMapper).insert(any(OrderCompleteTaskDO.class));
        verify(orderCompleteTaskMapper).updateById(updateCaptor.capture());
        assertEquals(OrderConstants.ORDER_COMPLETE_TASK_STATUS_RETRYING, updateCaptor.getValue().getTaskStatus());
        assertEquals(1, updateCaptor.getValue().getRetryCount());
        assertEquals("mq unavailable", updateCaptor.getValue().getLastErrorMessage());
    }

    /**
     * 构造订单完成事件。
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
