package com.example.ticket.user.support;

/**
 * 用户安全相关常量。
 * 当前承载用户服务在认证域内的稳定常量。
 * 包括 refresh token 存储前缀和统一 token 类型声明，避免业务代码散落字符串。
 */
public final class UserSecurityConstants {
    public static final String REFRESH_TOKEN_KEY_PREFIX = "ticket:user:refresh-token:";
    public static final String TOKEN_TYPE = "Bearer";

    /**
     * 禁止实例化常量类。
     */
    private UserSecurityConstants() {
    }
}
