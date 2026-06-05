package com.example.ticket.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.event.stock.StockEventConstants;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.domain.JobTicketOrderDO;
import com.example.ticket.job.gateway.StockReleaseEventPublisher;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.mapper.JobTicketOrderMapper;
import com.example.ticket.job.service.impl.ReservationRecheckServiceImpl;
import com.example.ticket.job.support.JobConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 预扣回查服务单元测试。
 * 用于固定“扫描超时预扣记录并按订单事实触发释放补偿”的业务边界。
 */
@ExtendWith(MockitoExtension.class)
class ReservationRecheckServiceTest {

    @Mock
    private JobReservationRecordMapper reservationRecordMapper;

    @Mock
    private JobTicketOrderMapper jobTicketOrderMapper;

    @Mock
    private StockReleaseEventPublisher stockReleaseEventPublisher;

    private ReservationRecheckService reservationRecheckService;

    /**
     * 构造被测预扣回查服务。
     * 当前测试通过 mock 持久化层与事件发布网关，隔离回查决策本身。
     */
    @BeforeEach
    void setUp() {
        reservationRecheckService = new ReservationRecheckServiceImpl(
                reservationRecordMapper,
                jobTicketOrderMapper,
                stockReleaseEventPublisher,
                100
        );
    }

    /**
     * 已过期且仍为 RESERVED、同时不存在订单事实时，应发布失败释放事件。
     */
    @Test
    void should_publish_create_failed_release_when_reserved_reservation_has_no_order() {
        JobReservationRecordDO record = buildReservedRecord();
        when(reservationRecordMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobReservationRecordDO>().setRecords(List.of(record)));
        when(jobTicketOrderMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobTicketOrderDO>().setRecords(List.of()));

        reservationRecheckService.recheckExpiredReservations(LocalDateTime.of(2026, 6, 5, 18, 0, 0));

        ArgumentCaptor<StockReleaseEvent> eventCaptor = ArgumentCaptor.forClass(StockReleaseEvent.class);
        verify(stockReleaseEventPublisher).publish(eventCaptor.capture());
        assertEquals(StockEventConstants.ORDER_CREATE_FAILED_RELEASE, eventCaptor.getValue().getEventType());
        assertEquals(JobConstants.STOCK_RELEASE_REASON_RESERVATION_RECHECK_EXPIRED, eventCaptor.getValue().getReason());
        assertEquals("reservation-001", eventCaptor.getValue().getReservationId());
    }

    /**
     * 已过期且处于 CONFIRMED、同时订单已关闭时，应发布超时释放事件。
     */
    @Test
    void should_publish_timeout_release_when_confirmed_reservation_belongs_to_closed_order() {
        JobReservationRecordDO record = buildReservedRecord();
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_CONFIRMED);
        when(reservationRecordMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobReservationRecordDO>().setRecords(List.of(record)));
        when(jobTicketOrderMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobTicketOrderDO>().setRecords(List.of(buildClosedOrder())));

        reservationRecheckService.recheckExpiredReservations(LocalDateTime.of(2026, 6, 5, 18, 0, 0));

        ArgumentCaptor<StockReleaseEvent> eventCaptor = ArgumentCaptor.forClass(StockReleaseEvent.class);
        verify(stockReleaseEventPublisher).publish(eventCaptor.capture());
        assertEquals(StockEventConstants.ORDER_TIMEOUT_RELEASE, eventCaptor.getValue().getEventType());
        assertEquals(JobConstants.STOCK_RELEASE_REASON_ORDER_TIMEOUT, eventCaptor.getValue().getReason());
        assertEquals("reservation-001", eventCaptor.getValue().getReservationId());
    }

    /**
     * 如果预扣仍停留在 RESERVED，但关联订单已经关闭，也应补发超时释放事件。
     * 这用于兜住“成功建单确认丢失，但后续超时关单已发生”的补偿重试场景。
     */
    @Test
    void should_publish_timeout_release_when_reserved_reservation_belongs_to_closed_order() {
        JobReservationRecordDO record = buildReservedRecord();
        when(reservationRecordMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobReservationRecordDO>().setRecords(List.of(record)));
        when(jobTicketOrderMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobTicketOrderDO>().setRecords(List.of(buildClosedOrder())));

        reservationRecheckService.recheckExpiredReservations(LocalDateTime.of(2026, 6, 5, 18, 0, 0));

        ArgumentCaptor<StockReleaseEvent> eventCaptor = ArgumentCaptor.forClass(StockReleaseEvent.class);
        verify(stockReleaseEventPublisher).publish(eventCaptor.capture());
        assertEquals(StockEventConstants.ORDER_TIMEOUT_RELEASE, eventCaptor.getValue().getEventType());
        assertEquals(JobConstants.STOCK_RELEASE_REASON_ORDER_TIMEOUT, eventCaptor.getValue().getReason());
    }

    /**
     * 如果关联订单仍未关闭，则不应提前触发释放，避免误补偿。
     */
    @Test
    void should_skip_release_when_related_order_is_still_created() {
        JobReservationRecordDO record = buildReservedRecord();
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_CONFIRMED);
        when(reservationRecordMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobReservationRecordDO>().setRecords(List.of(record)));
        JobTicketOrderDO order = buildClosedOrder();
        order.setOrderStatus(JobConstants.ORDER_STATUS_CREATED);
        when(jobTicketOrderMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<JobTicketOrderDO>().setRecords(List.of(order)));

        reservationRecheckService.recheckExpiredReservations(LocalDateTime.of(2026, 6, 5, 18, 0, 0));

        verify(stockReleaseEventPublisher, never()).publish(any(StockReleaseEvent.class));
    }

    /**
     * 构造已过期的 RESERVED 预扣记录。
     *
     * @return 预扣记录
     */
    private JobReservationRecordDO buildReservedRecord() {
        JobReservationRecordDO record = new JobReservationRecordDO();
        record.setReservationId("reservation-001");
        record.setOrderId(20001L);
        record.setRequestId("req-001");
        record.setIdempotencyKey("idem-001");
        record.setUserId(10001L);
        record.setActivityId(1001L);
        record.setTicketId(501L);
        record.setQuantity(1);
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_RESERVED);
        record.setSource(JobConstants.SOURCE_JOB_SERVICE);
        record.setExpireAt(LocalDateTime.of(2026, 6, 5, 17, 0, 0));
        return record;
    }

    /**
     * 构造已关闭订单。
     *
     * @return 已关闭订单
     */
    private JobTicketOrderDO buildClosedOrder() {
        JobTicketOrderDO order = new JobTicketOrderDO();
        order.setOrderId(20001L);
        order.setReservationId("reservation-001");
        order.setOrderStatus(JobConstants.ORDER_STATUS_CLOSED);
        return order;
    }
}
