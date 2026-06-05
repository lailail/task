package com.example.ticket.common.auth;

/**
 * JWT 声明字段常量。
 * 用于统一 token 内部的业务声明名称，避免 user-service 与 gateway-service 各自写死不同字段。
 */
public final class JwtClaimConstants {
    public static final String USERNAME = "username";
    public static final String DISPLAY_NAME = "displayName";
    public static final String TOKEN_TYPE = "tokenType";

    /**
     * 禁止实例化常量类。
     */
    private JwtClaimConstants() {
    }
}
