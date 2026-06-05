package com.example.ticket.order.service;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.event.stock.StockReleaseEvent;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.gateway.OrderStockReleaseEventPublisher;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.request.OrderCancelRequest;
import com.example.ticket.order.service.impl.OrderCancelServiceImpl;
import com.example.ticket.order.support.OrderConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单取消服务单元测试。
 * 用于固定“谁可以取消订单、什么状态允许取消、取消后必须释放库存”的核心业务边界。
 */
@ExtendWith(MockitoExtension.class)
class OrderCancelServiceTest {

    @Mock
    private TicketOrderMapper ticketOrderMapper;

    @Mock
    private OrderStockReleaseEventPublisher orderStockReleaseEventPublisher;

    private OrderCancelService orderCancelService;

    /**
     * 构造被测订单取消服务。
     */
    @BeforeEach
    void setUp() {
        orderCancelService = new OrderCancelServiceImpl(ticketOrderMapper, orderStockReleaseEventPublisher);
    }

    /**
     * 用户取消自己的 CREATED 订单时，应推进到 CANCELLED 并发布释放事件。
     */
    @Test
    void should_cancel_created_order_and_publish_release_when_owner_requests_cancel() {
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(10001L);
        OrderCancelRequest request = buildCancelRequest();
        when(ticketOrderMapper.selectById(20001L)).thenReturn(buildCreatedOrder());
        when(ticketOrderMapper.updateById(any(TicketOrderDO.class))).thenReturn(1);

        orderCancelService.cancelOrder(authenticatedUser, 20001L, request);

        ArgumentCaptor<TicketOrderDO> orderCaptor = ArgumentCaptor.forClass(TicketOrderDO.class);
        verify(ticketOrderMapper).updateById(orderCaptor.capture());
        assertEquals(OrderConstants.ORDER_STATUS_CANCELLED, orderCaptor.getValue().getOrderStatus());
        verify(orderStockReleaseEventPublisher).publish(any(StockReleaseEvent.class));
    }

    /**
     * 用户不能取消别人的订单。
     */
    @Test
    void should_reject_cancel_when_order_does_not_belong_to_user() {
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(10002L);
        OrderCancelRequest request = buildCancelRequest();
        when(ticketOrderMapper.selectById(20001L)).thenReturn(buildCreatedOrder());

        assertThrows(BusinessException.class, () -> orderCancelService.cancelOrder(authenticatedUser, 20001L, request));

        verify(ticketOrderMapper, never()).updateById(any(TicketOrderDO.class));
        verify(orderStockReleaseEventPublisher, never()).publish(any(StockReleaseEvent.class));
    }

    /**
     * 已支付订单不能再取消，避免覆盖付款后状态。
     */
    @Test
    void should_reject_cancel_when_order_is_already_paid() {
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(10001L);
        OrderCancelRequest request = buildCancelRequest();
        when(ticketOrderMapper.selectById(20001L)).thenReturn(buildPaidOrder());

        assertThrows(BusinessException.class, () -> orderCancelService.cancelOrder(authenticatedUser, 20001L, request));

        verify(ticketOrderMapper, never()).updateById(any(TicketOrderDO.class));
        verify(orderStockReleaseEventPublisher, never()).publish(any(StockReleaseEvent.class));
    }

    /**
     * 构造认证用户。
     *
     * @param userId 用户标识
     * @return 认证用户
     */
    private AuthenticatedUser buildAuthenticatedUser(Long userId) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(userId);
        authenticatedUser.setUsername("alice");
        authenticatedUser.setDisplayName("Alice");
        authenticatedUser.setTokenId("token-001");
        return authenticatedUser;
    }

    /**
     * 构造取消请求。
     *
     * @return 取消请求
     */
    private OrderCancelRequest buildCancelRequest() {
        OrderCancelRequest request = new OrderCancelRequest();
        request.setRequestId("cancel-req-001");
        request.setReason("USER_CANCEL");
        return request;
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
