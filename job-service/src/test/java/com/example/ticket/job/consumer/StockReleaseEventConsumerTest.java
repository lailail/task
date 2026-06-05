package com.example.ticket.job.consumer;

import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.service.ReservationReleaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

/**
 * 库存释放事件消费者单元测试。
 * 用于固定库存释放入口只负责接收事件并委托释放服务处理。
 */
@ExtendWith(MockitoExtension.class)
class StockReleaseEventConsumerTest {

    @Mock
    private ReservationReleaseService reservationReleaseService;

    /**
     * 消费到库存释放事件时，应委托预扣释放服务处理。
     */
    @Test
    void should_delegate_stock_release_event_to_reservation_release_service() {
        StockReleaseEventConsumer consumer = new StockReleaseEventConsumer(reservationReleaseService);
        StockReleaseEvent event = new StockReleaseEvent();
        event.setEventId("release-001");
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:30Z"));

        consumer.consume(event);

        verify(reservationReleaseService).handleStockRelease(event);
    }
}
