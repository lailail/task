package com.example.ticket.payment.gateway.dto;

/**
 * 订单状态 DTO。
 * 用于承接支付域从订单域内部查询接口拿到的最小状态结果。
 */
public class OrderStatusDTO {
    private Long orderId;
    private String orderStatus;

    /**
     * 获取订单标识。
     *
     * @return 订单标识
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * 设置订单标识。
     *
     * @param orderId 订单标识
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * 获取订单状态。
     *
     * @return 订单状态
     */
    public String getOrderStatus() {
        return orderStatus;
    }

    /**
     * 设置订单状态。
     *
     * @param orderStatus 订单状态
     */
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
