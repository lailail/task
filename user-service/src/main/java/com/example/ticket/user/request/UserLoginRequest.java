package com.example.ticket.user.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求对象。
 * 用于承载登录接口的输入参数和最小校验约束。
 */
public class UserLoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

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
     * 获取密码。
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码。
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
