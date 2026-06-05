package com.example.ticket.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.common.event.order.OrderCreateRequestedEvent;
import com.example.ticket.common.event.order.OrderCreateResultEvent;
import com.example.ticket.order.domain.OrderEventLogDO;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.gateway.OrderCreateResultEventPublisher;
import com.example.ticket.order.mapper.OrderEventLogMapper;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.service.impl.OrderCreateServiceImpl;
import com.example.ticket.order.support.OrderConstants;
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
 * 下单创建服务单元测试。
 * 用于固定 Phase 4 中“消费建单消息、幂等落库、发送订单结果事件”的业务边界。
 */
@ExtendWith(MockitoExtension.class)
class OrderCreateServiceTest {

    @Mock
    private TicketOrderMapper ticketOrderMapper;

    @Mock
    private OrderEventLogMapper orderEventLogMapper;

    @Mock
    private OrderCreateResultEventPublisher resultEventPublisher;

    private OrderCreateService orderCreateService;

    /**
     * 构造被测下单服务。
     * 当前测试通过 mock 持久化层和消息发布层，隔离订单编排规则。
     */
    @BeforeEach
    void setUp() {
        orderCreateService = new OrderCreateServiceImpl(
                ticketOrderMapper,
                orderEventLogMapper,
                resultEventPublisher,
                new ObjectMapper().findAndRegisterModules()
        );
    }

    /**
     * 首次收到下单请求事件时，应创建订单、记录事件日志并发送成功结果事件。
     */
    @Test
    void should_create_order_and_publish_created_result_when_event_is_first_seen() {
        OrderCreateRequestedEvent event = buildCreateEvent();
        when(orderEventLogMapper.selectOne(any())).thenReturn(null);
        when(ticketOrderMapper.selectOne(any())).thenReturn(null);
        when(ticketOrderMapper.insert(any(TicketOrderDO.class))).thenAnswer(invocation -> {
            TicketOrderDO order = invocation.getArgument(0);
            order.setOrderId(20001L);
            return 1;
        });
        when(orderEventLogMapper.insert(any(OrderEventLogDO.class))).thenReturn(1);

        orderCreateService.handleOrderCreateRequested(event);

        verify(ticketOrderMapper).insert(any(TicketOrderDO.class));
        verify(orderEventLogMapper).insert(any(OrderEventLogDO.class));

        ArgumentCaptor<OrderCreateResultEvent> resultCaptor = ArgumentCaptor.forClass(OrderCreateResultEvent.class);
        verify(resultEventPublisher).publish(resultCaptor.capture());
        assertEquals(OrderConstants.ORDER_RESULT_TYPE_CREATED, resultCaptor.getValue().getEventType());
        assertEquals("reservation-001", resultCaptor.getValue().getReservationId());
        assertEquals(20001L, resultCaptor.getValue().getOrderId());
    }

    /**
     * 当业务幂等订单已经存在时，不应重复建单，但仍应发送成功结果事件帮助后续状态收敛。
     */
    @Test
    void should_not_create_duplicate_order_when_business_order_already_exists() {
        OrderCreateRequestedEvent event = buildCreateEvent();
        when(orderEventLogMapper.selectOne(any())).thenReturn(null);
        when(ticketOrderMapper.selectOne(any())).thenReturn(buildExistingOrder());
        when(orderEventLogMapper.insert(any(OrderEventLogDO.class))).thenReturn(1);

        orderCreateService.handleOrderCreateRequested(event);

        verify(ticketOrderMapper, never()).insert(any(TicketOrderDO.class));
        verify(resultEventPublisher).publish(any(OrderCreateResultEvent.class));
    }

    /**
     * 当建单过程中发生异常时，应发送失败结果事件并保留失败事实，供后续补偿使用。
     */
    @Test
    void should_publish_failed_result_when_order_creation_throws_exception() {
        OrderCreateRequestedEvent event = buildCreateEvent();
        when(orderEventLogMapper.selectOne(any())).thenReturn(null);
        when(ticketOrderMapper.selectOne(any())).thenReturn(null);
        when(ticketOrderMapper.insert(any(TicketOrderDO.class))).thenThrow(new RuntimeException("db error"));
        when(orderEventLogMapper.insert(any(OrderEventLogDO.class))).thenReturn(1);

        orderCreateService.handleOrderCreateRequested(event);

        ArgumentCaptor<OrderCreateResultEvent> resultCaptor = ArgumentCaptor.forClass(OrderCreateResultEvent.class);
        verify(resultEventPublisher).publish(resultCaptor.capture());
        assertEquals(OrderConstants.ORDER_RESULT_TYPE_CREATE_FAILED, resultCaptor.getValue().getEventType());
        assertEquals("reservation-001", resultCaptor.getValue().getReservationId());
    }

    /**
     * 构造标准下单请求事件。
     *
     * @return 下单请求事件
     */
    private OrderCreateRequestedEvent buildCreateEvent() {
        OrderCreateRequestedEvent event = new OrderCreateRequestedEvent();
        event.setEventId("event-001");
        event.setEventType(OrderEventConstants.ORDER_CREATE_REQUESTED);
        event.setOccurredAt(Instant.parse("2026-06-05T12:00:00Z"));
        event.setRequestId("req-001");
        event.setIdempotencyKey("idem-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setExpireAt(Instant.parse("2026-06-05T12:15:00Z"));
        event.setSource(OrderEventConstants.SOURCE_SECKILL_SERVICE);
        return event;
    }

    /**
     * 构造已存在的订单对象。
     *
     * @return 已存在订单
     */
    private TicketOrderDO buildExistingOrder() {
        TicketOrderDO order = new TicketOrderDO();
        order.setOrderId(20001L);
        order.setReservationId("reservation-001");
        order.setIdempotencyKey("idem-001");
        order.setOrderStatus(OrderConstants.ORDER_STATUS_CREATED);
        return order;
    }
}
