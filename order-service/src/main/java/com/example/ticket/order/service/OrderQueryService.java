package com.example.ticket.order.service;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.order.request.UserOrderQueryRequest;
import com.example.ticket.order.response.OrderStatusResponse;
import com.example.ticket.order.response.ReservationResultResponse;
import com.example.ticket.order.response.UserOrderPageResponse;

/**
 * 订单查询服务接口。
 * 用于向内部调用方暴露最小订单状态事实，避免跨服务直接查询订单表。
 */
public interface OrderQueryService {

    /**
     * 查询订单状态。
     *
     * @param orderId 订单标识
     * @return 订单状态响应
     */
    OrderStatusResponse queryOrderStatus(Long orderId);

    /**
     * 查询当前用户自己的订单分页。
     *
     * @param authenticatedUser 当前认证用户
     * @param request 分页查询请求
     * @return 用户订单分页
     */
    UserOrderPageResponse queryCurrentUserOrders(AuthenticatedUser authenticatedUser, UserOrderQueryRequest request);

    /**
     * 查询当前用户某次抢票结果。
     *
     * @param authenticatedUser 当前认证用户
     * @param reservationId 预扣标识
     * @return 抢票结果感知响应
     */
    ReservationResultResponse queryReservationResult(AuthenticatedUser authenticatedUser, String reservationId);
}
