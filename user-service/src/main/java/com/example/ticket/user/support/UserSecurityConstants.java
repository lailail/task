package com.example.ticket.user.support;

/**
 * 用户安全相关常量。
 * 当前只承载 Phase 2 占位登录能力的最小常量，后续接正式鉴权方案时可继续集中演进。
 */
public final class UserSecurityConstants {

    public static final String DEMO_ACCESS_TOKEN_PREFIX = "token-";

    /**
     * 禁止实例化常量类。
     */
    private UserSecurityConstants() {
    }
}
