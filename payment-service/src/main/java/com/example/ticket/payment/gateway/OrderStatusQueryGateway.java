package com.example.ticket.payment.gateway;

import com.example.ticket.payment.gateway.dto.OrderStatusDTO;

/**
 * 订单状态查询网关。
 * 用于支付域在对账时回查订单域当前状态。
 */
public interface OrderStatusQueryGateway {

    /**
     * 查询订单状态。
     *
     * @param orderId 订单标识
     * @return 订单状态
     */
    OrderStatusDTO queryOrderStatus(Long orderId);
}
