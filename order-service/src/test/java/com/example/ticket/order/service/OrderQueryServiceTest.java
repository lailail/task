package com.example.ticket.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.order.domain.OrderReservationRecordDO;
import com.example.ticket.order.domain.TicketOrderDO;
import com.example.ticket.order.mapper.OrderReservationRecordMapper;
import com.example.ticket.order.mapper.TicketOrderMapper;
import com.example.ticket.order.request.UserOrderQueryRequest;
import com.example.ticket.order.response.ReservationResultResponse;
import com.example.ticket.order.response.UserOrderPageResponse;
import com.example.ticket.order.service.impl.OrderQueryServiceImpl;
import com.example.ticket.order.support.OrderConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单查询服务单元测试。
 * 用于固定用户侧订单分页与抢票结果感知的查询边界。
 */
@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    private TicketOrderMapper ticketOrderMapper;

    @Mock
    private OrderReservationRecordMapper reservationRecordMapper;

    private OrderQueryService orderQueryService;

    /**
     * 构造被测订单查询服务。
     */
    @BeforeEach
    void setUp() {
        orderQueryService = new OrderQueryServiceImpl(ticketOrderMapper, reservationRecordMapper);
    }

    /**
     * 查询我的订单时，应按当前登录用户分页返回，不能由前端传 userId。
     */
    @Test
    void should_query_current_user_orders_with_normalized_paging() {
        UserOrderQueryRequest request = new UserOrderQueryRequest();
        request.setCurrent(0L);
        request.setPageSize(500L);
        Page<TicketOrderDO> mapperPage = new Page<>(1L, OrderConstants.MAX_USER_ORDER_PAGE_SIZE);
        mapperPage.setRecords(List.of(buildOrder(20001L, 10001L, "reservation-001")));
        mapperPage.setTotal(1L);
        when(ticketOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mapperPage);

        UserOrderPageResponse response = orderQueryService.queryCurrentUserOrders(buildUser(10001L), request);

        assertEquals(1L, response.getCurrent());
        assertEquals(OrderConstants.MAX_USER_ORDER_PAGE_SIZE, response.getPageSize());
        assertEquals(1L, response.getTotal());
        assertEquals(1, response.getRecords().size());
        assertEquals(20001L, response.getRecords().get(0).getOrderId());
        ArgumentCaptor<Page<TicketOrderDO>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(ticketOrderMapper).selectPage(pageCaptor.capture(), any(LambdaQueryWrapper.class));
        assertEquals(1L, pageCaptor.getValue().getCurrent());
        assertEquals(OrderConstants.MAX_USER_ORDER_PAGE_SIZE, pageCaptor.getValue().getSize());
    }

    /**
     * 预扣存在但订单尚未创建时，应返回处理中语义，不能误判为失败。
     */
    @Test
    void should_return_reserved_result_when_reservation_exists_but_order_not_created() {
        when(reservationRecordMapper.selectById("reservation-001"))
                .thenReturn(buildReservation("reservation-001", 10001L));
        when(ticketOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ReservationResultResponse response =
                orderQueryService.queryReservationResult(buildUser(10001L), "reservation-001");

        assertEquals("reservation-001", response.getReservationId());
        assertEquals(OrderConstants.USER_RESULT_STATUS_RESERVED, response.getResultStatus());
        assertNull(response.getOrderId());
    }

    /**
     * 订单已经创建时，应返回订单事实和面向用户的结果状态。
     */
    @Test
    void should_return_order_result_when_order_is_created_for_current_user() {
        when(reservationRecordMapper.selectById("reservation-001"))
                .thenReturn(buildReservation("reservation-001", 10001L));
        when(ticketOrderMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(buildOrder(20001L, 10001L, "reservation-001"));

        ReservationResultResponse response =
                orderQueryService.queryReservationResult(buildUser(10001L), "reservation-001");

        assertEquals(OrderConstants.USER_RESULT_STATUS_ORDER_CREATED, response.getResultStatus());
        assertEquals(20001L, response.getOrderId());
        assertEquals(OrderConstants.ORDER_STATUS_CREATED, response.getOrderStatus());
    }

    /**
     * 当前用户查询别人的预扣结果时，应返回未找到语义。
     */
    @Test
    void should_return_not_found_when_reservation_belongs_to_other_user() {
        when(reservationRecordMapper.selectById("reservation-001"))
                .thenReturn(buildReservation("reservation-001", 10002L));

        ReservationResultResponse response =
                orderQueryService.queryReservationResult(buildUser(10001L), "reservation-001");

        assertEquals(OrderConstants.USER_RESULT_STATUS_NOT_FOUND, response.getResultStatus());
        assertNull(response.getOrderId());
    }

    /**
     * 构造认证用户。
     *
     * @param userId 用户标识
     * @return 认证用户
     */
    private AuthenticatedUser buildUser(Long userId) {
        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserId(userId);
        user.setUsername("alice");
        user.setDisplayName("Alice");
        user.setTokenId("token-001");
        return user;
    }

    /**
     * 构造预扣记录。
     *
     * @param reservationId 预扣标识
     * @param userId 用户标识
     * @return 预扣记录
     */
    private OrderReservationRecordDO buildReservation(String reservationId, Long userId) {
        OrderReservationRecordDO record = new OrderReservationRecordDO();
        record.setReservationId(reservationId);
        record.setUserId(userId);
        record.setActivityId(1001L);
        record.setTicketId(501L);
        record.setQuantity(1);
        record.setReservationStatus("RESERVED");
        record.setExpireAt(LocalDateTime.now().plusMinutes(15));
        return record;
    }

    /**
     * 构造订单记录。
     *
     * @param orderId 订单标识
     * @param userId 用户标识
     * @param reservationId 预扣标识
     * @return 订单记录
     */
    private TicketOrderDO buildOrder(Long orderId, Long userId, String reservationId) {
        TicketOrderDO order = new TicketOrderDO();
        order.setOrderId(orderId);
        order.setOrderNo("ORD-" + orderId);
        order.setReservationId(reservationId);
        order.setRequestId("req-001");
        order.setIdempotencyKey("idem-001");
        order.setUserId(userId);
        order.setActivityId(1001L);
        order.setTicketId(501L);
        order.setQuantity(1);
        order.setAmountCent(68000);
        order.setOrderStatus(OrderConstants.ORDER_STATUS_CREATED);
        order.setExpireAt(LocalDateTime.now().plusMinutes(15));
        return order;
    }
}
