package com.example.ticket.job.consumer;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.job.service.ReservationConfirmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

/**
 * 订单结果事件消费者单元测试。
 * 用于固定预扣状态收敛入口只负责接收结果事件并委托应用服务。
 */
@ExtendWith(MockitoExtension.class)
class OrderResultEventConsumerTest {

    @Mock
    private ReservationConfirmService reservationConfirmService;

    /**
     * 消费到订单结果事件时，应委托预扣确认服务处理。
     */
    @Test
    void should_delegate_order_result_event_to_reservation_confirm_service() {
        OrderResultEventConsumer consumer = new OrderResultEventConsumer(reservationConfirmService);
        OrderCreateResultEvent event = new OrderCreateResultEvent();
        event.setEventId("result-001");
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:10Z"));

        consumer.consume(event);

        verify(reservationConfirmService).handleOrderCreateResult(event);
    }
}
