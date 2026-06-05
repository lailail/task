package com.example.ticket.job.service;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.gateway.StockReleaseEventPublisher;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.service.impl.ReservationReleaseTriggerServiceImpl;
import com.example.ticket.job.support.JobConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 预扣释放触发服务单元测试。
 * 用于固定“建单失败后必须发布库存释放事件”的业务边界。
 */
@ExtendWith(MockitoExtension.class)
class ReservationReleaseTriggerServiceTest {

    @Mock
    private JobReservationRecordMapper reservationRecordMapper;

    @Mock
    private StockReleaseEventPublisher stockReleaseEventPublisher;

    private ReservationReleaseTriggerService reservationReleaseTriggerService;

    /**
     * 构造被测释放触发服务。
     * 当前测试通过 mock 持久化层和事件发布层，隔离失败结果到释放事件的编排规则。
     */
    @BeforeEach
    void setUp() {
        reservationReleaseTriggerService = new ReservationReleaseTriggerServiceImpl(
                reservationRecordMapper,
                stockReleaseEventPublisher
        );
    }

    /**
     * 收到建单失败结果且预扣仍处于 RESERVED 时，应发布库存释放事件。
     */
    @Test
    void should_publish_stock_release_event_when_order_creation_failed() {
        OrderCreateResultEvent event = buildFailedResult();
        when(reservationRecordMapper.selectById("reservation-001")).thenReturn(buildReservedRecord());

        reservationReleaseTriggerService.handleOrderCreateResult(event);

        ArgumentCaptor<StockReleaseEvent> eventCaptor = ArgumentCaptor.forClass(StockReleaseEvent.class);
        verify(stockReleaseEventPublisher).publish(eventCaptor.capture());
        assertEquals(StockEventConstants.ORDER_CREATE_FAILED_RELEASE, eventCaptor.getValue().getEventType());
        assertEquals("reservation-001", eventCaptor.getValue().getReservationId());
        assertEquals("DB_ERROR", eventCaptor.getValue().getReason());
    }

    /**
     * 如果预扣记录已经不再是 RESERVED，则不应重复触发库存释放。
     */
    @Test
    void should_skip_stock_release_when_reservation_is_already_confirmed() {
        OrderCreateResultEvent event = buildFailedResult();
        JobReservationRecordDO record = buildReservedRecord();
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_CONFIRMED);
        when(reservationRecordMapper.selectById("reservation-001")).thenReturn(record);

        reservationReleaseTriggerService.handleOrderCreateResult(event);

        verify(stockReleaseEventPublisher, never()).publish(any(StockReleaseEvent.class));
    }

    /**
     * 构造建单失败结果事件。
     *
     * @return 建单失败结果事件
     */
    private OrderCreateResultEvent buildFailedResult() {
        OrderCreateResultEvent event = new OrderCreateResultEvent();
        event.setEventId("result-002");
        event.setEventType(JobConstants.ORDER_RESULT_TYPE_CREATE_FAILED);
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:20Z"));
        event.setRequestId("req-001");
        event.setIdempotencyKey("idem-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setReason("DB_ERROR");
        return event;
    }

    /**
     * 构造处于 RESERVED 状态的预扣记录。
     *
     * @return 预扣记录
     */
    private JobReservationRecordDO buildReservedRecord() {
        JobReservationRecordDO record = new JobReservationRecordDO();
        record.setReservationId("reservation-001");
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_RESERVED);
        return record;
    }
}
