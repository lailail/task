package com.example.ticket.order.response;

/**
 * 订单状态响应。
 * 用于向支付域等内部调用方返回订单主状态，避免暴露整张订单表结构。
 */
public class OrderStatusResponse {
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
