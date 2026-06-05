package com.example.ticket.user.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌请求对象。
 * 用于承载调用方提交的 refresh token，避免刷新接口继续复用登录入参。
 */
public class UserRefreshTokenRequest {
    @NotBlank
    private String refreshToken;

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
}
