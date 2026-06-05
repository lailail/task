package com.example.ticket.job.service;

import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.job.domain.JobReservationRecordDO;
import com.example.ticket.job.mapper.JobReservationRecordMapper;
import com.example.ticket.job.service.impl.ReservationConfirmServiceImpl;
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
 * 预扣确认服务单元测试。
 * 用于固定订单结果事件驱动预扣状态收敛的规则。
 */
@ExtendWith(MockitoExtension.class)
class ReservationConfirmServiceTest {

    @Mock
    private JobReservationRecordMapper reservationRecordMapper;

    private ReservationConfirmService reservationConfirmService;

    /**
     * 构造被测预扣确认服务。
     * 当前测试通过 mock 持久化层验证状态流转约束。
     */
    @BeforeEach
    void setUp() {
        reservationConfirmService = new ReservationConfirmServiceImpl(reservationRecordMapper);
    }

    /**
     * 收到订单创建成功结果时，应把预扣记录推进到 CONFIRMED 并回写订单标识。
     */
    @Test
    void should_confirm_reservation_when_order_created_result_arrives() {
        OrderCreateResultEvent event = buildCreatedResult();
        when(reservationRecordMapper.selectById("reservation-001")).thenReturn(buildReservedRecord());

        reservationConfirmService.handleOrderCreateResult(event);

        ArgumentCaptor<JobReservationRecordDO> recordCaptor = ArgumentCaptor.forClass(JobReservationRecordDO.class);
        verify(reservationRecordMapper).updateById(recordCaptor.capture());
        assertEquals(JobConstants.RESERVATION_STATUS_CONFIRMED, recordCaptor.getValue().getReservationStatus());
        assertEquals(20001L, recordCaptor.getValue().getOrderId());
    }

    /**
     * 如果预扣记录已经不是 RESERVED，则不应重复推进状态。
     */
    @Test
    void should_skip_confirmation_when_reservation_is_already_confirmed() {
        OrderCreateResultEvent event = buildCreatedResult();
        JobReservationRecordDO record = buildReservedRecord();
        record.setReservationStatus(JobConstants.RESERVATION_STATUS_CONFIRMED);
        when(reservationRecordMapper.selectById("reservation-001")).thenReturn(record);

        reservationConfirmService.handleOrderCreateResult(event);

        verify(reservationRecordMapper, never()).updateById(any(JobReservationRecordDO.class));
    }

    /**
     * 收到订单创建失败结果时，当前阶段不确认预扣记录，后续补偿放到 Phase 5。
     */
    @Test
    void should_not_confirm_reservation_when_order_creation_failed() {
        OrderCreateResultEvent event = buildFailedResult();

        reservationConfirmService.handleOrderCreateResult(event);

        verify(reservationRecordMapper, never()).selectById(any());
        verify(reservationRecordMapper, never()).updateById(any(JobReservationRecordDO.class));
    }

    /**
     * 构造订单创建成功结果事件。
     *
     * @return 结果事件
     */
    private OrderCreateResultEvent buildCreatedResult() {
        OrderCreateResultEvent event = new OrderCreateResultEvent();
        event.setEventId("result-001");
        event.setEventType(JobConstants.ORDER_RESULT_TYPE_CREATED);
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:10Z"));
        event.setRequestId("req-001");
        event.setIdempotencyKey("idem-001");
        event.setReservationId("reservation-001");
        event.setOrderId(20001L);
        return event;
    }

    /**
     * 构造订单创建失败结果事件。
     *
     * @return 结果事件
     */
    private OrderCreateResultEvent buildFailedResult() {
        OrderCreateResultEvent event = new OrderCreateResultEvent();
        event.setEventId("result-002");
        event.setEventType(JobConstants.ORDER_RESULT_TYPE_CREATE_FAILED);
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:20Z"));
        event.setRequestId("req-001");
        event.setIdempotencyKey("idem-001");
        event.setReservationId("reservation-001");
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
