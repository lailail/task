package com.example.ticket.order.controller;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.common.web.AuthenticatedUserHeaderSupport;
import com.example.ticket.order.request.OrderCancelRequest;
import com.example.ticket.order.request.UserOrderQueryRequest;
import com.example.ticket.order.response.OrderStatusResponse;
import com.example.ticket.order.response.ReservationResultResponse;
import com.example.ticket.order.response.UserOrderPageResponse;
import com.example.ticket.order.service.OrderCancelService;
import com.example.ticket.order.service.OrderQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口控制器。
 * 用于承接用户侧订单查询、订单取消和内部订单状态查询入口，并把认证后的请求转交给服务层。
 */
@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderCancelService orderCancelService;
    private final OrderQueryService orderQueryService;

    /**
     * 构造订单控制器。
     *
     * @param orderCancelService 订单取消服务
     * @param orderQueryService 订单查询服务
     */
    public OrderController(OrderCancelService orderCancelService, OrderQueryService orderQueryService) {
        this.orderCancelService = orderCancelService;
        this.orderQueryService = orderQueryService;
    }

    /**
     * 查询当前用户自己的订单分页。
     *
     * @param request 分页查询请求
     * @param httpServletRequest HTTP 请求
     * @return 用户订单分页响应
     */
    @GetMapping("/orders")
    public ApiResponse<UserOrderPageResponse> queryCurrentUserOrders(
            UserOrderQueryRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthenticatedUser authenticatedUser =
                AuthenticatedUserHeaderSupport.extractAuthenticatedUser(httpServletRequest);
        log.info("收到用户订单分页查询请求，userId={}, current={}, pageSize={}",
                authenticatedUser.getUserId(), request.getCurrent(), request.getPageSize());
        UserOrderPageResponse response = orderQueryService.queryCurrentUserOrders(authenticatedUser, request);
        log.info("用户订单分页查询完成，userId={}, current={}, pageSize={}, total={}",
                authenticatedUser.getUserId(), response.getCurrent(), response.getPageSize(), response.getTotal());
        return ApiResponse.success(response);
    }

    /**
     * 查询当前用户某次抢票结果。
     *
     * @param reservationId 预扣标识
     * @param httpServletRequest HTTP 请求
     * @return 抢票结果感知响应
     */
    @GetMapping("/orders/reservations/{reservationId}")
    public ApiResponse<ReservationResultResponse> queryReservationResult(
            @PathVariable String reservationId,
            HttpServletRequest httpServletRequest
    ) {
        AuthenticatedUser authenticatedUser =
                AuthenticatedUserHeaderSupport.extractAuthenticatedUser(httpServletRequest);
        log.info("收到用户抢票结果查询请求，userId={}, reservationId={}",
                authenticatedUser.getUserId(), reservationId);
        ReservationResultResponse response = orderQueryService.queryReservationResult(authenticatedUser, reservationId);
        log.info("用户抢票结果查询完成，userId={}, reservationId={}, resultStatus={}",
                authenticatedUser.getUserId(), reservationId, response.getResultStatus());
        return ApiResponse.success(response);
    }

    /**
     * 取消当前用户自己的待支付订单。
     *
     * @param orderId 订单标识
     * @param request 取消请求
     * @param httpServletRequest HTTP 请求
     * @return 通用成功响应
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCancelRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthenticatedUser authenticatedUser =
                AuthenticatedUserHeaderSupport.extractAuthenticatedUser(httpServletRequest);
        log.info("收到取消订单请求，requestId={}, orderId={}, userId={}, reason={}",
                request.getRequestId(), orderId, authenticatedUser.getUserId(), request.getReason());
        orderCancelService.cancelOrder(authenticatedUser, orderId, request);
        log.info("取消订单请求处理完成，requestId={}, orderId={}, userId={}",
                request.getRequestId(), orderId, authenticatedUser.getUserId());
        return ApiResponse.success(null);
    }

    /**
     * 查询订单当前状态。
     *
     * @param orderId 订单标识
     * @return 订单状态响应
     */
    @GetMapping("/internal/orders/{orderId}/status")
    public ApiResponse<OrderStatusResponse> queryOrderStatus(@PathVariable Long orderId) {
        log.info("收到订单状态查询请求，orderId={}", orderId);
        OrderStatusResponse response = orderQueryService.queryOrderStatus(orderId);
        log.info("订单状态查询完成，orderId={}, orderStatus={}", orderId, response.getOrderStatus());
        return ApiResponse.success(response);
    }
}
