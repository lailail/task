package com.example.ticket.order.service;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import com.example.ticket.common.event.payment.PaymentResultEvent;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.gateway.OrderStockReleaseEventPublisher;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.service.impl.OrderPaymentServiceImpl;
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
 * 订单支付结果处理服务单元测试。
 * 用于固定“支付结果进入订单域后，如何驱动 PAID/CANCELLED 状态流转与库存释放”的核心约束。
 */
@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceTest {

    @Mock
    private TicketOrderMapper ticketOrderMapper;

    @Mock
    private OrderStockReleaseEventPublisher orderStockReleaseEventPublisher;

    private OrderPaymentService orderPaymentService;

    /**
     * 构造被测订单支付结果处理服务。
     */
    @BeforeEach
    void setUp() {
        orderPaymentService = new OrderPaymentServiceImpl(ticketOrderMapper, orderStockReleaseEventPublisher);
    }

    /**
     * 支付成功且订单仍处于 CREATED 时，应推进到 PAID。
     */
    @Test
    void should_mark_order_paid_when_payment_succeeds_for_created_order() {
        PaymentResultEvent event = buildPaymentResultEvent(PaymentEventConstants.PAYMENT_SUCCEEDED);
        when(ticketOrderMapper.selectById(20001L)).thenReturn(buildCreatedOrder());
        when(ticketOrderMapper.updateById(any(TicketOrderDO.class))).thenReturn(1);

        orderPaymentService.handlePaymentResult(event);

        ArgumentCaptor<TicketOrderDO> orderCaptor = ArgumentCaptor.forClass(TicketOrderDO.class);
        verify(ticketOrderMapper).updateById(orderCaptor.capture());
        assertEquals(OrderConstants.ORDER_STATUS_PAID, orderCaptor.getValue().getOrderStatus());
        verify(orderStockReleaseEventPublisher, never()).publish(any(StockReleaseEvent.class));
    }

    /**
     * 支付过期且订单仍处于 CREATED 时，应推进到 CANCELLED 并触发库存释放。
     */
    @Test
    void should_cancel_order_and_publish_release_when_payment_expires() {
        PaymentResultEvent event = buildPaymentResultEvent(PaymentEventConstants.PAYMENT_EXPIRED);
        when(ticketOrderMapper.selectById(20001L)).thenReturn(buildCreatedOrder());
        when(ticketOrderMapper.updateById(any(TicketOrderDO.class))).thenReturn(1);

        orderPaymentService.handlePaymentResult(event);

        ArgumentCaptor<TicketOrderDO> orderCaptor = ArgumentCaptor.forClass(TicketOrderDO.class);
        verify(ticketOrderMapper).updateById(orderCaptor.capture());
        assertEquals(OrderConstants.ORDER_STATUS_CANCELLED, orderCaptor.getValue().getOrderStatus());
        verify(orderStockReleaseEventPublisher).publish(any(StockReleaseEvent.class));
    }

    /**
     * 如果订单已经是 PAID，则重复支付结果不应再次改写状态。
     */
    @Test
    void should_ignore_duplicate_payment_result_when_order_is_already_paid() {
        PaymentResultEvent event = buildPaymentResultEvent(PaymentEventConstants.PAYMENT_SUCCEEDED);
        when(ticketOrderMapper.selectById(20001L)).thenReturn(buildPaidOrder());

        orderPaymentService.handlePaymentResult(event);

        verify(ticketOrderMapper, never()).updateById(any(TicketOrderDO.class));
        verify(orderStockReleaseEventPublisher, never()).publish(any(StockReleaseEvent.class));
    }

    /**
     * 构造支付结果事件。
     *
     * @param eventType 支付事件类型
     * @return 支付结果事件
     */
    private PaymentResultEvent buildPaymentResultEvent(String eventType) {
        PaymentResultEvent event = new PaymentResultEvent();
        event.setEventId("payment-event-001");
        event.setEventType(eventType);
        event.setOccurredAt(Instant.parse("2026-06-05T13:00:00Z"));
        event.setOrderId(20001L);
        event.setOrderNo("ORD-001");
        event.setRequestId("req-001");
        event.setIdempotencyKey("pay-idem-001");
        event.setReservationId("reservation-001");
        event.setActivityId(1001L);
        event.setTicketId(501L);
        event.setUserId(10001L);
        event.setQuantity(1);
        event.setSource(PaymentEventConstants.SOURCE_PAYMENT_SERVICE);
        return event;
    }

    /**
     * 构造待支付订单。
     *
     * @return 待支付订单
     */
    private TicketOrderDO buildCreatedOrder() {
        TicketOrderDO order = new TicketOrderDO();
        order.setOrderId(20001L);
        order.setOrderNo("ORD-001");
        order.setReservationId("reservation-001");
        order.setRequestId("req-001");
        order.setIdempotencyKey("idem-001");
        order.setUserId(10001L);
        order.setActivityId(1001L);
        order.setTicketId(501L);
        order.setQuantity(1);
        order.setOrderStatus(OrderConstants.ORDER_STATUS_CREATED);
        return order;
    }

    /**
     * 构造已支付订单。
     *
     * @return 已支付订单
     */
    private TicketOrderDO buildPaidOrder() {
        TicketOrderDO order = buildCreatedOrder();
        order.setOrderStatus(OrderConstants.ORDER_STATUS_PAID);
        return order;
    }
}
