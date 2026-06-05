package com.example.ticket.user.response;

import java.time.Instant;

/**
 * 用户登录响应对象。
 * 用于向调用方返回正式 JWT 登录结果和令牌有效期信息。
 */
public class UserLoginResponse {
    private Long userId;
    private String username;
    private String displayName;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Instant accessTokenExpireAt;
    private Instant refreshTokenExpireAt;

    /**
     * 获取用户主键。
     *
     * @return 用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户主键。
     *
     * @param userId 用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取展示名称。
     *
     * @return 展示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置展示名称。
     *
     * @param displayName 展示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取访问令牌。
     *
     * @return 访问令牌
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 设置访问令牌。
     *
     * @param accessToken 访问令牌
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * 获取刷新令牌。
     *
     * @return 刷新令牌
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * 设置刷新令牌。
     *
     * @param refreshToken 刷新令牌
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 获取令牌类型。
     *
     * @return 令牌类型
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * 设置令牌类型。
     *
     * @param tokenType 令牌类型
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * 获取访问令牌过期时间。
     *
     * @return 访问令牌过期时间
     */
    public Instant getAccessTokenExpireAt() {
        return accessTokenExpireAt;
    }

    /**
     * 设置访问令牌过期时间。
     *
     * @param accessTokenExpireAt 访问令牌过期时间
     */
    public void setAccessTokenExpireAt(Instant accessTokenExpireAt) {
        this.accessTokenExpireAt = accessTokenExpireAt;
    }

    /**
     * 获取刷新令牌过期时间。
     *
     * @return 刷新令牌过期时间
     */
    public Instant getRefreshTokenExpireAt() {
        return refreshTokenExpireAt;
    }

    /**
     * 设置刷新令牌过期时间。
     *
     * @param refreshTokenExpireAt 刷新令牌过期时间
     */
    public void setRefreshTokenExpireAt(Instant refreshTokenExpireAt) {
        this.refreshTokenExpireAt = refreshTokenExpireAt;
    }
}
