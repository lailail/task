package com.example.ticket.payment.gateway.impl;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.payment.gateway.OrderStatusQueryGateway;
import com.example.ticket.payment.gateway.dto.OrderStatusDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 基于 HTTP 的订单状态查询网关。
 * 用于在最小实现阶段通过内部接口回查订单状态，而不是跨服务共享订单表。
 */
@Component
public class RestOrderStatusQueryGateway implements OrderStatusQueryGateway {
    private final RestClient restClient;
    private final String orderStatusPath;

    /**
     * 构造订单状态查询网关。
     *
     * @param restClientBuilder RestClient 构造器
     * @param orderServiceBaseUrl 订单服务基础地址
     * @param orderStatusPath 订单状态查询路径模板
     */
    public RestOrderStatusQueryGateway(
            RestClient.Builder restClientBuilder,
            @Value("${ticket.payment.order-service-base-url:http://127.0.0.1:8084}") String orderServiceBaseUrl,
            @Value("${ticket.payment.order-status-path:/api/v1/internal/orders/{orderId}/status}") String orderStatusPath
    ) {
        this.restClient = restClientBuilder.baseUrl(orderServiceBaseUrl).build();
        this.orderStatusPath = orderStatusPath;
    }

    /**
     * 查询订单状态。
     *
     * @param orderId 订单标识
     * @return 订单状态 DTO
     */
    @Override
    public OrderStatusDTO queryOrderStatus(Long orderId) {
        try {
            ApiResponse<OrderStatusDTO> response = restClient.get()
                    .uri(orderStatusPath, orderId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return response == null ? null : response.getData();
        } catch (RestClientException exception) {
            // 对账场景允许订单域暂时不可用，后续由下次调度继续重试。
            return null;
        }
    }
}
