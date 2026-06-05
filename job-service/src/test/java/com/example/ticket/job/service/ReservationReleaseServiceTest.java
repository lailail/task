package com.example.ticket.job.service;

import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.gateway.StockReleaseGateway;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.service.impl.ReservationReleaseServiceImpl;
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
 * 预扣释放服务单元测试。
 * 用于固定“释放事件到达后执行 Redis 回补并把预扣状态收敛到 RELEASED”的业务边界。
 */
@ExtendWith(MockitoExtension.class)
class ReservationReleaseServiceTest {

    @Mock
    private JobReservationRecordMapper reservationRecordMapper;

    @Mock
    private StockReleaseGateway stockReleaseGateway;

    private ReservationReleaseService reservationReleaseService;

    /**
     * 构造被测预扣释放服务。
     * 当前测试通过 mock Redis 网关和持久化层，隔离释放编排与状态收敛规则。
     */
    @BeforeEach
    void setUp() {
        reservationReleaseService = new ReservationReleaseServiceImpl(
                reservationRecordMapper,
                stockReleaseGateway
        );
    }

    /**
     * 收到释放事件且 Redis 回补成功时，应把预扣记录推进到 RELEASED。
     */
    @Test
    void should_release_reservation_when_stock_release_event_arrives() {
        StockReleaseEvent event = buildReleaseEvent();
        when(reservationRecordMapper.selectById("reservation-001")).thenReturn(buildReservedRecord());
        when(stockReleaseGateway.release(event)).thenReturn(true);

        reservationReleaseService.handleStockRelease(event);

        ArgumentCaptor<JobReservationRecordDO> recordCaptor = ArgumentCaptor.forClass(JobReservationRecordDO.class);
        verify(reservationRecordMapper).updateById(recordCaptor.capture());
        assertEquals(JobConstants.RESERVATION_STATUS_RELEASED, recordCaptor.getValue().getReservationStatus());
        assertEquals("DB_ERROR", recordCaptor.getValue().getReason());
    }

    /**
     * 收到超时释放事件且预扣已处于 CONFIRMED 时，也应允许进入 RELEASED。
     */
    @Test
    void should_release_confirmed_reservation_when_timeout_release_event_arrives() {
        StockReleaseEvent event = buildTimeoutReleaseEvent();
        JobReservationRecordDO record = buildReservedRecord();
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_CONFIRMED);
        when(reservationRecordMapper.selectById("reservation-001")).thenReturn(record);
        when(stockReleaseGateway.release(event)).thenReturn(true);

        reservationReleaseService.handleStockRelease(event);

        ArgumentCaptor<JobReservationRecordDO> recordCaptor = ArgumentCaptor.forClass(JobReservationRecordDO.class);
        verify(reservationRecordMapper).updateById(recordCaptor.capture());
        assertEquals(JobConstants.RESERVATION_STATUS_RELEASED, recordCaptor.getValue().getReservationStatus());
        assertEquals("ORDER_TIMEOUT", recordCaptor.getValue().getReason());
    }

    /**
     * 如果预扣记录已经释放，则不应重复执行 Redis 回补和状态更新。
     */
    @Test
    void should_skip_release_when_reservation_is_already_released() {
        StockReleaseEvent event = buildReleaseEvent();
        JobReservationRecordDO record = buildReservedRecord();
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_RELEASED);
        when(reservationRecordMapper.selectById("reservation-001")).thenReturn(record);

        reservationReleaseService.handleStockRelease(event);

        verify(stockReleaseGateway, never()).release(any(StockReleaseEvent.class));
        verify(reservationRecordMapper, never()).updateById(any(JobReservationRecordDO.class));
    }

    /**
     * 构造库存释放事件。
     *
     * @return 库存释放事件
     */
    private StockReleaseEvent buildReleaseEvent() {
        StockReleaseEvent event = new StockReleaseEvent();
        event.setEventId("release-001");
        event.setEventType(StockEventConstants.ORDER_CREATE_FAILED_RELEASE);
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:30Z"));
        event.setRequestId("req-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setReason("DB_ERROR");
        event.setSource(JobConstants.SOURCE_JOB_SERVICE);
        return event;
    }

    /**
     * 构造订单超时释放事件。
     *
     * @return 库存释放事件
     */
    private StockReleaseEvent buildTimeoutReleaseEvent() {
        StockReleaseEvent event = buildReleaseEvent();
        event.setEventType(StockEventConstants.ORDER_TIMEOUT_RELEASE);
        event.setReason("ORDER_TIMEOUT");
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
