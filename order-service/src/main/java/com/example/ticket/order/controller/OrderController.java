package com.example.ticket.order.controller;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.common.web.AuthenticatedUserHeaderSupport;
import com.example.ticket.order.request.OrderCancelRequest;
import com.example.ticket.order.response.OrderStatusResponse;
import com.example.ticket.order.service.OrderCancelService;
import com.example.ticket.order.service.OrderQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口控制器。
 * 用于承接主动取消订单与内部订单状态查询入口，并把认证、校验后的请求转交给服务层。
 */
@RestController
@RequestMapping("/api/v1")
public class OrderController {
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
        orderCancelService.cancelOrder(authenticatedUser, orderId, request);
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
        return ApiResponse.success(orderQueryService.queryOrderStatus(orderId));
    }
}
