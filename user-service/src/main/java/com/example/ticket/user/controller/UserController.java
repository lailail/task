package com.example.ticket.user.controller;

import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.user.dto.UserDTO;
import com.example.ticket.user.request.UserLoginRequest;
import com.example.ticket.user.request.UserRegisterRequest;
import com.example.ticket.user.response.UserLoginResponse;
import com.example.ticket.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口控制器。
 * 当前阶段只负责接收注册和登录请求，并把参数校验后的调用转交给服务层。
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
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
        return ApiResponse.success(userService.register(request));
    }

    /**
     * 校验用户名密码并返回当前阶段的占位登录结果。
     */
    @PostMapping("/login")
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }
}
