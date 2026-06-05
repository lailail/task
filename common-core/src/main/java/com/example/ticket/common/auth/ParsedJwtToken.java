package com.example.ticket.common.auth;

import java.time.Instant;

/**
 * 已解析 JWT 对象。
 * 用于承接网关和用户服务从 token 中提取出的稳定认证事实。
 */
public class ParsedJwtToken {
    private AuthenticatedUser authenticatedUser;
    private JwtTokenType tokenType;
    private Instant issuedAt;
    private Instant expireAt;

    /**
     * 获取已认证用户。
     *
     * @return 已认证用户
     */
    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    /**
     * 设置已认证用户。
     *
     * @param authenticatedUser 已认证用户
     */
    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * 获取令牌类型。
     *
     * @return 令牌类型
     */
    public JwtTokenType getTokenType() {
        return tokenType;
    }

    /**
     * 设置令牌类型。
     *
     * @param tokenType 令牌类型
     */
    public void setTokenType(JwtTokenType tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * 获取签发时间。
     *
     * @return 签发时间
     */
    public Instant getIssuedAt() {
        return issuedAt;
    }

    /**
     * 设置签发时间。
     *
     * @param issuedAt 签发时间
     */
    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    /**
     * 获取过期时间。
     *
     * @return 过期时间
     */
    public Instant getExpireAt() {
        return expireAt;
    }

    /**
     * 设置过期时间。
     *
     * @param expireAt 过期时间
     */
    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }
}
