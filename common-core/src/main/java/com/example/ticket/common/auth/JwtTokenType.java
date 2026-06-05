package com.example.ticket.common.auth;

/**
 * JWT 令牌类型枚举。
 * 用于区分访问令牌和刷新令牌，避免同一令牌被跨用途误用。
 */
public enum JwtTokenType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    /**
     * 构造令牌类型枚举项。
     *
     * @param value 类型值
     */
    JwtTokenType(String value) {
        this.value = value;
    }

    /**
     * 获取令牌类型值。
     *
     * @return 类型值
     */
    public String getValue() {
        return value;
    }

    /**
     * 根据字符串解析令牌类型。
     *
     * @param value 类型值
     * @return 令牌类型
     */
    public static JwtTokenType fromValue(String value) {
        for (JwtTokenType tokenType : values()) {
            if (tokenType.value.equals(value)) {
                return tokenType;
            }
        }
        return null;
    }
}
