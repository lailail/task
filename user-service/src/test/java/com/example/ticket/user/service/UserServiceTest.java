package com.example.ticket.user.service;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.user.domain.UserDO;
import com.example.ticket.user.dto.UserDTO;
import com.example.ticket.user.repository.UserRepository;
import com.example.ticket.user.request.UserLoginRequest;
import com.example.ticket.user.request.UserRegisterRequest;
import com.example.ticket.user.response.UserLoginResponse;
import com.example.ticket.user.service.impl.UserServiceImpl;
import com.example.ticket.user.support.UserSecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 用户服务单元测试。
 * 用于验证用户注册和登录编排逻辑不依赖具体仓储实现。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    private UserService service;

    /**
     * 构造被测用户服务。
     * 当前测试仅关注业务编排，因此仓储通过接口 mock 隔离。
     */
    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(repository, new BCryptPasswordEncoder());
    }

    /**
     * 用户名未占用时，应成功注册并返回脱敏用户信息。
     */
    @Test
    void should_register_user_when_username_is_new() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("alice");
        request.setPassword("password123");
        request.setDisplayName("Alice");
        when(repository.findByUsername("alice")).thenReturn(Optional.empty());
        when(repository.save(any(UserDO.class))).thenAnswer(invocation -> {
            UserDO user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });

        UserDTO user = service.register(request);

        assertEquals("alice", user.getUsername());
        assertEquals("Alice", user.getDisplayName());
        assertTrue(user.getUserId() > 0);
    }

    /**
     * 用户名已存在时，应返回稳定业务错误。
     */
    @Test
    void should_reject_register_when_username_already_exists() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("alice");
        request.setPassword("password123");
        request.setDisplayName("Alice");
        when(repository.findByUsername("alice")).thenReturn(Optional.of(buildUser("alice", "encoded-password", "Alice")));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(request));
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS.getCode(), exception.getCode());
    }

    /**
     * 凭证正确时，应返回占位登录结果。
     */
    @Test
    void should_login_when_credentials_are_valid() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("password123");
        when(repository.findByUsername("alice")).thenReturn(Optional.of(buildEncodedUser("alice", "password123", "Alice")));

        UserLoginResponse response = service.login(loginRequest);

        assertEquals("alice", response.getUsername());
        assertTrue(response.getUserId() > 0);
        assertTrue(response.getAccessToken() != null && !response.getAccessToken().isBlank());
        assertTrue(response.getAccessToken().startsWith(UserSecurityConstants.DEMO_ACCESS_TOKEN_PREFIX));
    }

    /**
     * 密码错误时，应返回稳定凭证错误。
     */
    @Test
    void should_reject_login_when_password_is_invalid() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("wrong-password");
        when(repository.findByUsername("alice")).thenReturn(Optional.of(buildEncodedUser("alice", "password123", "Alice")));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(loginRequest));
        assertEquals(ErrorCode.INVALID_CREDENTIALS.getCode(), exception.getCode());
    }

    /**
     * 构造普通用户对象。
     *
     * @param username 用户名
     * @param password 密码
     * @param displayName 展示名称
     * @return 用户对象
     */
    private UserDO buildUser(String username, String password, String displayName) {
        UserDO user = new UserDO();
        user.setUserId(1L);
        user.setUsername(username);
        user.setPassword(password);
        user.setDisplayName(displayName);
        return user;
    }

    /**
     * 构造带加密密码的用户对象。
     *
     * @param username 用户名
     * @param rawPassword 原始密码
     * @param displayName 展示名称
     * @return 用户对象
     */
    private UserDO buildEncodedUser(String username, String rawPassword, String displayName) {
        return buildUser(username, new BCryptPasswordEncoder().encode(rawPassword), displayName);
    }
}
