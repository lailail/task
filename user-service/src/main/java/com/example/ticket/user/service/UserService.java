package com.example.ticket.user.service;

import com.example.ticket.user.dto.UserDTO;
import com.example.ticket.user.request.UserLoginRequest;
import com.example.ticket.user.request.UserRefreshTokenRequest;
import com.example.ticket.user.request.UserRegisterRequest;
import com.example.ticket.user.response.UserLoginResponse;

/**
 * 用户领域服务接口。
 * 定义用户注册和登录能力的对外契约。
 */
public interface UserService {

    /**
     * 注册用户。
     *
     * @param request 注册请求
     * @return 注册后的用户信息
     */
    UserDTO register(UserRegisterRequest request);

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    UserLoginResponse login(UserLoginRequest request);

    /**
     * 根据 refresh token 刷新登录结果。
     *
     * @param request 刷新令牌请求
     * @return 新的登录结果
     */
    UserLoginResponse refreshToken(UserRefreshTokenRequest request);
}
