package com.example.ticket.user.service.impl;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.user.domain.UserDO;
import com.example.ticket.user.dto.UserDTO;
import com.example.ticket.user.repository.UserRepository;
import com.example.ticket.user.request.UserLoginRequest;
import com.example.ticket.user.request.UserRegisterRequest;
import com.example.ticket.user.response.UserLoginResponse;
import com.example.ticket.user.service.UserService;
import com.example.ticket.user.support.UserSecurityConstants;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户领域服务实现。
 * 当前阶段负责注册和登录主流程编排，持久化仍通过仓储接口抽象，避免业务层直接耦合具体存储实现。
 */
@Service
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * 构造用户服务实现。
     *
     * @param userRepository 用户仓储
     * @param passwordEncoder 密码编码器
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建新用户并返回脱敏后的用户信息。
     */
    @Override
    public UserDTO register(UserRegisterRequest request) {
        // 用户名在当前阶段被视为唯一登录标识，注册前必须先走一次显式存在性校验。
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        UserDO user = new UserDO();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());

        UserDO savedUser = userRepository.save(user);
        return toUserDTO(savedUser);
    }

    /**
     * 校验用户凭证并返回当前阶段的占位登录结果。
     */
    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        // 登录校验必须同时满足“用户存在”和“密码匹配”，统一失败为凭证错误，避免泄露更多账户信息。
        UserDO user = userRepository.findByUsername(request.getUsername())
                .filter(item -> passwordEncoder.matches(request.getPassword(), item.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        UserLoginResponse response = new UserLoginResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        // 当前仍是 Phase 2 占位 token，实现目标只是打通最小登录链路，后续会替换为正式 JWT 方案。
        response.setAccessToken(UserSecurityConstants.DEMO_ACCESS_TOKEN_PREFIX + UUID.randomUUID());
        return response;
    }

    /**
     * 把持久化对象转换成对外返回的用户 DTO，避免把内部密码等字段暴露给调用方。
     */
    private UserDTO toUserDTO(UserDO user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        return dto;
    }
}
