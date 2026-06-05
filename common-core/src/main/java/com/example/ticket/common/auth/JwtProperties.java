package com.example.ticket.common.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 * 用于统一约束签发者、密钥和过期时间等认证参数，避免在代码中散落硬编码。
 */
@ConfigurationProperties(prefix = "ticket.security.jwt")
public class JwtProperties {
    private String issuer = "ticket-system";
    private String secret = "ticket-local-demo-jwt-secret-key-please-change";
    private long accessTokenExpireSeconds = 1800;
    private long refreshTokenExpireSeconds = 604800;

    /**
     * 获取签发者。
     *
     * @return 签发者
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * 设置签发者。
     *
     * @param issuer 签发者
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * 获取签名密钥。
     *
     * @return 签名密钥
     */
    public String getSecret() {
        return secret;
    }

    /**
     * 设置签名密钥。
     *
     * @param secret 签名密钥
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * 获取访问令牌有效期。
     *
     * @return 访问令牌有效期秒数
     */
    public long getAccessTokenExpireSeconds() {
        return accessTokenExpireSeconds;
    }

    /**
     * 设置访问令牌有效期。
     *
     * @param accessTokenExpireSeconds 访问令牌有效期秒数
     */
    public void setAccessTokenExpireSeconds(long accessTokenExpireSeconds) {
        this.accessTokenExpireSeconds = accessTokenExpireSeconds;
    }

    /**
     * 获取刷新令牌有效期。
     *
     * @return 刷新令牌有效期秒数
     */
    public long getRefreshTokenExpireSeconds() {
        return refreshTokenExpireSeconds;
    }

    /**
     * 设置刷新令牌有效期。
     *
     * @param refreshTokenExpireSeconds 刷新令牌有效期秒数
     */
    public void setRefreshTokenExpireSeconds(long refreshTokenExpireSeconds) {
        this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
    }
}
