package com.example.ticket.order.service;

import com.example.ticket.order.response.OrderStatusResponse;

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
}
