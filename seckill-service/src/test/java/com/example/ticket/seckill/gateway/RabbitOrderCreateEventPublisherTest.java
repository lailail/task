package com.example.ticket.seckill.gateway;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.seckill.gateway.impl.RabbitOrderCreateEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

/**
 * Rabbit 下单事件发布器单元测试。
 * 用于固定下单事件发送的交换机、路由键和载荷透传约定。
 */
@ExtendWith(MockitoExtension.class)
class RabbitOrderCreateEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitOrderCreateEventPublisher publisher;

    /**
     * 构造被测发布器。
     * 通过显式实例化方式固定测试关注点只在 MQ 路由契约。
     */
    @BeforeEach
    void setUp() {
        publisher = new RabbitOrderCreateEventPublisher(rabbitTemplate);
    }

    /**
     * 发布下单事件时，应使用约定的交换机与路由键。
     */
    @Test
    void should_publish_event_to_expected_exchange_and_routing_key() {
        OrderCreateRequestedEvent event = buildEvent();

        publisher.publish(event);

        ArgumentCaptor<OrderCreateRequestedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreateRequestedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(OrderEventConstants.ORDER_CREATE_EXCHANGE),
                org.mockito.ArgumentMatchers.eq(OrderEventConstants.ORDER_CREATE_ROUTING_KEY),
                eventCaptor.capture()
        );
        assertEquals("reservation-001", eventCaptor.getValue().getReservationId());
        assertEquals("idem-001", eventCaptor.getValue().getIdempotencyKey());
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
