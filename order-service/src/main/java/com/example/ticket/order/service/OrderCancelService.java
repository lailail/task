package com.example.ticket.order.service;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.order.request.OrderCancelRequest;

/**
 * 订单取消服务接口。
 * 用于收敛主动取消入口的权限校验、状态校验和库存释放触发逻辑。
 */
public interface OrderCancelService {

    /**
     * 取消指定订单。
     *
     * @param authenticatedUser 当前认证用户
     * @param orderId 订单标识
     * @param request 取消请求
     */
    void cancelOrder(AuthenticatedUser authenticatedUser, Long orderId, OrderCancelRequest request);
}
