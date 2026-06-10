package com.example.ticket.order.controller;

import com.example.ticket.common.auth.AuthHeaderConstants;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.order.request.UserOrderQueryRequest;
import com.example.ticket.order.response.ReservationResultResponse;
import com.example.ticket.order.response.UserOrderPageResponse;
import com.example.ticket.order.service.OrderCancelService;
import com.example.ticket.order.service.OrderQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单控制器测试。
 * 用于固定用户侧查询接口必须从网关透传头读取身份，不信任前端传入用户标识。
 */
class OrderControllerTest {

    /**
     * 查询我的订单时，应使用当前认证用户并返回统一响应。
     */
    @Test
    void should_query_current_user_orders_with_authenticated_header() {
        OrderCancelService cancelService = mock(OrderCancelService.class);
        OrderQueryService queryService = mock(OrderQueryService.class);
        OrderController controller = new OrderController(cancelService, queryService);
        UserOrderPageResponse pageResponse = new UserOrderPageResponse();
        pageResponse.setRecords(List.of());
        when(queryService.queryCurrentUserOrders(any(), any(UserOrderQueryRequest.class))).thenReturn(pageResponse);

        ApiResponse<UserOrderPageResponse> response =
                controller.queryCurrentUserOrders(new UserOrderQueryRequest(), buildRequest(10001L));

        assertEquals(0, response.getCode());
        assertEquals(pageResponse, response.getData());
        verify(queryService).queryCurrentUserOrders(any(), any(UserOrderQueryRequest.class));
    }

    /**
     * 查询单次抢票结果时，应使用当前认证用户和 reservationId。
     */
    @Test
    void should_query_reservation_result_with_authenticated_header() {
        OrderCancelService cancelService = mock(OrderCancelService.class);
        OrderQueryService queryService = mock(OrderQueryService.class);
        OrderController controller = new OrderController(cancelService, queryService);
        ReservationResultResponse resultResponse = new ReservationResultResponse();
        resultResponse.setReservationId("reservation-001");
        when(queryService.queryReservationResult(any(), eq("reservation-001"))).thenReturn(resultResponse);

        ApiResponse<ReservationResultResponse> response =
                controller.queryReservationResult("reservation-001", buildRequest(10001L));

        assertEquals(0, response.getCode());
        assertEquals(resultResponse, response.getData());
        verify(queryService).queryReservationResult(any(), eq("reservation-001"));
    }

    /**
     * 构造带网关认证头的请求。
     *
     * @param userId 用户标识
     * @return HTTP 请求
     */
    private HttpServletRequest buildRequest(Long userId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(AuthHeaderConstants.AUTHENTICATED_USER_ID)).thenReturn(String.valueOf(userId));
        when(request.getHeader(AuthHeaderConstants.AUTHENTICATED_USERNAME)).thenReturn("alice");
        when(request.getHeader(AuthHeaderConstants.AUTHENTICATED_DISPLAY_NAME)).thenReturn("Alice");
        when(request.getHeader(AuthHeaderConstants.AUTHENTICATED_TOKEN_ID)).thenReturn("token-001");
        return request;
    }
}
