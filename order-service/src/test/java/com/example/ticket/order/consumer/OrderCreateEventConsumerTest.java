package com.example.ticket.order.consumer;

import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.order.service.OrderCreateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

/**
 * 下单请求事件消费者单元测试。
 * 用于固定 MQ 消费入口只负责接收事件并委托应用服务处理。
 */
@ExtendWith(MockitoExtension.class)
class OrderCreateEventConsumerTest {

    @Mock
    private OrderCreateService orderCreateService;

    /**
     * 消费到下单请求事件时，应委托给应用服务处理。
     */
    @Test
    void should_delegate_order_create_event_to_application_service() {
        OrderCreateEventConsumer consumer = new OrderCreateEventConsumer(orderCreateService);
        OrderCreateRequestedEvent event = new OrderCreateRequestedEvent();
        event.setEventId("event-001");
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:00Z"));

        consumer.consume(event);

        verify(orderCreateService).handleOrderCreateRequested(event);
    }
}
