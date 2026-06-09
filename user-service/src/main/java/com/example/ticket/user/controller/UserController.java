package com.example.ticket.user.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.user.dto.UserDTO;
import com.example.ticket.user.request.UserLoginRequest;
import com.example.ticket.user.request.UserRefreshTokenRequest;
import com.example.ticket.user.request.UserRegisterRequest;
import com.example.ticket.user.response.UserLoginResponse;
import com.example.ticket.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口控制器。
 * 当前阶段负责接收注册、登录和刷新令牌请求，并把参数校验后的调用转交给服务层。
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * 构造用户控制器。
     *
     * @param userService 用户服务
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册用户并返回最小用户信息。
     */
    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("收到用户注册请求，username={}", request.getUsername());
        UserDTO userDTO = userService.register(request);
        log.info("用户注册成功，userId={}, username={}", userDTO.getUserId(), userDTO.getUsername());
        return ApiResponse.success(userDTO);
    }

    /**
     * 校验用户名密码并返回正式 JWT 登录结果。
     */
    @PostMapping("/login")
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("收到用户登录请求，username={}", request.getUsername());
        UserLoginResponse response = userService.login(request);
        log.info("用户登录成功，userId={}, username={}", response.getUserId(), response.getUsername());
        return ApiResponse.success(response);
    }

    /**
     * 根据 refresh token 换取新的登录结果。
     *
     * @param request 刷新令牌请求
     * @return 新的登录结果
     */
    @PostMapping("/token/refresh")
    public ApiResponse<UserLoginResponse> refreshToken(@Valid @RequestBody UserRefreshTokenRequest request) {
        log.info("收到刷新令牌请求");
        UserLoginResponse response = userService.refreshToken(request);
        log.info("刷新令牌成功，userId={}, username={}", response.getUserId(), response.getUsername());
        return ApiResponse.success(response);
    }
}
