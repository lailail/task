package com.example.ticket.common.auth;

/**
 * 认证请求头常量。
 * 用于约束网关向下游透传的统一身份头名称，避免各服务自行发明不同字段。
 */
public final class AuthHeaderConstants {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHENTICATED_USER_ID = "X-Authenticated-User-Id";
    public static final String AUTHENTICATED_USERNAME = "X-Authenticated-Username";
    public static final String AUTHENTICATED_DISPLAY_NAME = "X-Authenticated-Display-Name";
    public static final String AUTHENTICATED_TOKEN_ID = "X-Authenticated-Token-Id";

    /**
     * 禁止实例化常量类。
     */
    private AuthHeaderConstants() {
    }
}
