package com.example.ticket.user.service;

import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.auth.JwtTokenSupport;
import com.example.ticket.common.auth.JwtTokenType;
import com.example.ticket.common.auth.ParsedJwtToken;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.user.domain.UserDO;
import com.example.ticket.user.dto.UserDTO;
import com.example.ticket.user.repository.RefreshTokenStore;
import com.example.ticket.user.repository.UserRepository;
import com.example.ticket.user.request.UserLoginRequest;
import com.example.ticket.user.request.UserRefreshTokenRequest;
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

    @Mock
    private JwtTokenSupport jwtTokenSupport;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    private UserService service;

    /**
     * 构造被测用户服务。
     * 当前测试仅关注业务编排，因此仓储通过接口 mock 隔离。
     */
    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(repository, new BCryptPasswordEncoder(), jwtTokenSupport, refreshTokenStore);
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
     * 凭证正确时，应返回正式 JWT 登录结果。
     */
    @Test
    void should_login_when_credentials_are_valid() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("password123");
        when(repository.findByUsername("alice")).thenReturn(Optional.of(buildEncodedUser("alice", "password123", "Alice")));
        mockJwtIssue("access-token", "refresh-token");

        UserLoginResponse response = service.login(loginRequest);

        assertEquals("alice", response.getUsername());
        assertTrue(response.getUserId() > 0);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(UserSecurityConstants.TOKEN_TYPE, response.getTokenType());
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
     * refresh token 合法且仍在存储中时，应返回新的登录结果。
     */
    @Test
    void should_refresh_tokens_when_refresh_token_is_valid() {
        UserRefreshTokenRequest request = new UserRefreshTokenRequest();
        request.setRefreshToken("refresh-token");
        when(repository.findById(1L)).thenReturn(Optional.of(buildEncodedUser("alice", "password123", "Alice")));
        when(refreshTokenStore.exists(1L, "refresh-jti")).thenReturn(true);
        when(jwtTokenSupport.parseToken("refresh-token")).thenReturn(buildParsedRefreshToken("refresh-jti"));
        mockJwtIssue("new-access-token", "new-refresh-token");

        UserLoginResponse response = service.refreshToken(request);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    /**
     * refresh token 不在存储中时，应返回稳定错误。
     */
    @Test
    void should_reject_refresh_when_refresh_token_is_not_stored() {
        UserRefreshTokenRequest request = new UserRefreshTokenRequest();
        request.setRefreshToken("refresh-token");
        when(jwtTokenSupport.parseToken("refresh-token")).thenReturn(buildParsedRefreshToken("refresh-jti"));
        when(refreshTokenStore.exists(1L, "refresh-jti")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.refreshToken(request));
        assertEquals(ErrorCode.REFRESH_TOKEN_INVALID.getCode(), exception.getCode());
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

    /**
     * 构造刷新令牌解析结果。
     *
     * @param tokenId 令牌唯一标识
     * @return 解析结果
     */
    private ParsedJwtToken buildParsedRefreshToken(String tokenId) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(1L);
        authenticatedUser.setUsername("alice");
        authenticatedUser.setDisplayName("Alice");
        authenticatedUser.setTokenId(tokenId);

        ParsedJwtToken parsedJwtToken = new ParsedJwtToken();
        parsedJwtToken.setAuthenticatedUser(authenticatedUser);
        parsedJwtToken.setTokenType(JwtTokenType.REFRESH);
        return parsedJwtToken;
    }

    /**
     * 模拟 JWT 签发流程。
     *
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     */
    private void mockJwtIssue(String accessToken, String refreshToken) {
        when(jwtTokenSupport.createAccessToken(any(AuthenticatedUser.class))).thenReturn(accessToken);
        when(jwtTokenSupport.createRefreshToken(any(AuthenticatedUser.class))).thenReturn(refreshToken);
        when(jwtTokenSupport.parseToken(accessToken)).thenReturn(buildParsedAccessToken("access-jti-issued"));
        when(jwtTokenSupport.parseToken(refreshToken)).thenReturn(buildParsedRefreshToken("refresh-jti-issued"));
    }

    /**
     * 构造访问令牌解析结果。
     *
     * @param tokenId 令牌唯一标识
     * @return 解析结果
     */
    private ParsedJwtToken buildParsedAccessToken(String tokenId) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(1L);
        authenticatedUser.setUsername("alice");
        authenticatedUser.setDisplayName("Alice");
        authenticatedUser.setTokenId(tokenId);

        ParsedJwtToken parsedJwtToken = new ParsedJwtToken();
        parsedJwtToken.setAuthenticatedUser(authenticatedUser);
        parsedJwtToken.setTokenType(JwtTokenType.ACCESS);
        return parsedJwtToken;
    }
}
