package com.example.ticket.seckill.service;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.seckill.dto.SeckillActivityDTO;
import com.example.ticket.seckill.gateway.OrderCreateEventPublisher;
import com.example.ticket.seckill.gateway.StockReservationGateway;
import com.example.ticket.seckill.gateway.model.StockReserveCommand;
import com.example.ticket.seckill.gateway.model.StockReserveResult;
import com.example.ticket.seckill.repository.SeckillActivityRepository;
import com.example.ticket.seckill.request.SeckillReserveRequest;
import com.example.ticket.seckill.response.SeckillReserveResponse;
import com.example.ticket.seckill.service.impl.SeckillServiceImpl;
import com.example.ticket.seckill.support.SeckillConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 抢票服务单元测试。
 * 用于验证 Phase 3 第一段主链路的业务编排边界。
 */
@ExtendWith(MockitoExtension.class)
class SeckillServiceTest {

    @Mock
    private SeckillActivityRepository activityRepository;

    @Mock
    private StockReservationGateway stockReservationGateway;

    @Mock
    private OrderCreateEventPublisher orderCreateEventPublisher;

    private SeckillServiceImpl seckillService;

    /**
     * 按测试约束手动构造被测服务。
     * 避免使用 Mockito 在带配置参数构造器场景下误判依赖装配。
     */
    @BeforeEach
    void setUp() {
        seckillService = new SeckillServiceImpl(
                activityRepository,
                stockReservationGateway,
                orderCreateEventPublisher,
                900L
        );
    }

    /**
     * 成功预扣库存后，应返回预扣结果并发送下单事件。
     */
    @Test
    void should_reserve_stock_and_publish_order_event_when_request_is_valid() {
        SeckillReserveRequest request = buildRequest();
        SeckillActivityDTO activity = buildActivity(SeckillConstants.SALE_STATUS_ON_SALE);
        StockReserveResult result = StockReserveResult.success(
                "reservation-001",
                Instant.parse("2026-06-05T10:00:00Z"),
                Instant.parse("2026-06-05T10:15:00Z")
        );

        when(activityRepository.findByActivityIdAndTicketId(request.getActivityId(), request.getTicketId()))
                .thenReturn(Optional.of(activity));
        when(stockReservationGateway.reserve(any(StockReserveCommand.class))).thenReturn(result);

        SeckillReserveResponse response = seckillService.reserve(request);

        assertEquals("reservation-001", response.getReservationId());
        assertEquals(SeckillConstants.RESERVATION_STATUS_RESERVED, response.getStatus());

        ArgumentCaptor<OrderCreateRequestedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreateRequestedEvent.class);
        verify(orderCreateEventPublisher).publish(eventCaptor.capture());
        assertEquals("reservation-001", eventCaptor.getValue().getReservationId());
        assertEquals(request.getIdempotencyKey(), eventCaptor.getValue().getIdempotencyKey());
    }

    /**
     * 活动不存在时，应直接返回稳定业务错误。
     */
    @Test
    void should_throw_when_activity_is_missing() {
        SeckillReserveRequest request = buildRequest();
        when(activityRepository.findByActivityIdAndTicketId(request.getActivityId(), request.getTicketId()))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> seckillService.reserve(request));

        assertEquals(ErrorCode.SECKILL_ACTIVITY_NOT_FOUND.getCode(), exception.getCode());
        verify(stockReservationGateway, never()).reserve(any());
        verify(orderCreateEventPublisher, never()).publish(any());
    }

    /**
     * 活动未开售时，应拦截预扣请求。
     */
    @Test
    void should_throw_when_activity_is_not_on_sale() {
        SeckillReserveRequest request = buildRequest();
        when(activityRepository.findByActivityIdAndTicketId(request.getActivityId(), request.getTicketId()))
                .thenReturn(Optional.of(buildActivity(SeckillConstants.SALE_STATUS_COMING_SOON)));

        BusinessException exception = assertThrows(BusinessException.class, () -> seckillService.reserve(request));

        assertEquals(ErrorCode.SECKILL_ACTIVITY_NOT_ON_SALE.getCode(), exception.getCode());
        verify(stockReservationGateway, never()).reserve(any());
        verify(orderCreateEventPublisher, never()).publish(any());
    }

    /**
     * 重复请求或库存不足时，应返回对应业务错误且不发送消息。
     */
    @Test
    void should_throw_when_reserve_result_is_not_success() {
        SeckillReserveRequest request = buildRequest();
        when(activityRepository.findByActivityIdAndTicketId(request.getActivityId(), request.getTicketId()))
                .thenReturn(Optional.of(buildActivity(SeckillConstants.SALE_STATUS_ON_SALE)));
        when(stockReservationGateway.reserve(any(StockReserveCommand.class)))
                .thenReturn(StockReserveResult.failure(SeckillConstants.RESERVE_RESULT_DUPLICATE, null));

        BusinessException exception = assertThrows(BusinessException.class, () -> seckillService.reserve(request));

        assertEquals(ErrorCode.SECKILL_DUPLICATE_REQUEST.getCode(), exception.getCode());
        verify(orderCreateEventPublisher, never()).publish(any());
    }

    /**
     * 构造标准抢票请求。
     *
     * @return 标准抢票请求
     */
    private SeckillReserveRequest buildRequest() {
        SeckillReserveRequest request = new SeckillReserveRequest();
        request.setUserId(10001L);
        request.setActivityId(1001L);
        request.setTicketId(501L);
        request.setQuantity(1);
        request.setIdempotencyKey("idem-001");
        request.setRequestId("req-001");
        return request;
    }

    /**
     * 构造演示活动对象。
     *
     * @param saleStatus 销售状态
     * @return 活动对象
     */
    private SeckillActivityDTO buildActivity(String saleStatus) {
        SeckillActivityDTO activity = new SeckillActivityDTO();
        activity.setActivityId(1001L);
        activity.setTicketId(501L);
        activity.setActivityName("五月天上海演唱会");
        activity.setSaleStatus(saleStatus);
        activity.setAvailableStock(100);
        return activity;
    }
}
