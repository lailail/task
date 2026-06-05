package com.example.ticket.order.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 订单取消请求。
 * 用于承接用户主动取消订单时必须携带的请求标识与业务原因。
 */
public class OrderCancelRequest {
    @NotBlank(message = "requestId 不能为空")
    private String requestId;

    @NotBlank(message = "reason 不能为空")
    private String reason;

    /**
     * 获取请求标识。
     *
     * @return 请求标识
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 设置请求标识。
     *
     * @param requestId 请求标识
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 获取取消原因。
     *
     * @return 取消原因
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置取消原因。
     *
     * @param reason 取消原因
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}
