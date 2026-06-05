package com.example.ticket.user.service;

import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.user.dto.UserDTO;
import com.example.ticket.user.repository.InMemoryUserRepository;
import com.example.ticket.user.request.UserLoginRequest;
import com.example.ticket.user.request.UserRegisterRequest;
import com.example.ticket.user.response.UserLoginResponse;
import com.example.ticket.user.service.impl.UserServiceImpl;
import com.example.ticket.user.support.UserSecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private InMemoryUserRepository repository;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        service = new UserServiceImpl(repository, new BCryptPasswordEncoder());
    }

    @Test
    void should_register_user_when_username_is_new() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("alice");
        request.setPassword("password123");
        request.setDisplayName("Alice");

        UserDTO user = service.register(request);

        assertEquals("alice", user.getUsername());
        assertEquals("Alice", user.getDisplayName());
        assertTrue(user.getUserId() > 0);
    }

    @Test
    void should_reject_register_when_username_already_exists() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("alice");
        request.setPassword("password123");
        request.setDisplayName("Alice");

        service.register(request);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(request));
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS.getCode(), exception.getCode());
    }

    @Test
    void should_login_when_credentials_are_valid() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("alice");
        registerRequest.setPassword("password123");
        registerRequest.setDisplayName("Alice");
        service.register(registerRequest);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("password123");

        UserLoginResponse response = service.login(loginRequest);

        assertEquals("alice", response.getUsername());
        assertTrue(response.getUserId() > 0);
        assertTrue(response.getAccessToken() != null && !response.getAccessToken().isBlank());
        assertTrue(response.getAccessToken().startsWith(UserSecurityConstants.DEMO_ACCESS_TOKEN_PREFIX));
    }

    @Test
    void should_reject_login_when_password_is_invalid() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("alice");
        registerRequest.setPassword("password123");
        registerRequest.setDisplayName("Alice");
        service.register(registerRequest);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("wrong-password");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(loginRequest));
        assertEquals(ErrorCode.INVALID_CREDENTIALS.getCode(), exception.getCode());
    }
}
