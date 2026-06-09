package com.example.ticket.user.service.impl;

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
import com.example.ticket.user.service.UserService;
import com.example.ticket.user.support.UserSecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户领域服务实现。
 * 当前阶段负责注册、登录、刷新令牌主流程编排，持久化仍通过仓储接口抽象，避免业务层直接耦合具体存储实现。
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenSupport jwtTokenSupport;
    private final RefreshTokenStore refreshTokenStore;

    /**
     * 构造用户服务实现。
     *
     * @param userRepository 用户仓储
     * @param passwordEncoder 密码编码器
     * @param jwtTokenSupport JWT 支持组件
     * @param refreshTokenStore 刷新令牌存储
     */
    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenSupport jwtTokenSupport,
            RefreshTokenStore refreshTokenStore
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenSupport = jwtTokenSupport;
        this.refreshTokenStore = refreshTokenStore;
    }

    /**
     * 创建新用户并返回脱敏后的用户信息。
     */
    @Override
    public UserDTO register(UserRegisterRequest request) {
        // 用户名在当前阶段被视为唯一登录标识，注册前必须先走一次显式存在性校验。
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("用户注册命中重复用户名，username={}", request.getUsername());
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        UserDO user = new UserDO();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());

        UserDO savedUser = userRepository.save(user);
        log.info("用户注册已落库，userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());
        return toUserDTO(savedUser);
    }

    /**
     * 校验用户凭证并返回正式 JWT 登录结果。
     */
    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        // 登录校验必须同时满足“用户存在”和“密码匹配”，统一失败为凭证错误，避免泄露更多账户信息。
        UserDO user = userRepository.findByUsername(request.getUsername())
                .filter(item -> passwordEncoder.matches(request.getPassword(), item.getPassword()))
                .orElseThrow(() -> {
                    log.warn("用户登录失败，username={}", request.getUsername());
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });
        log.info("用户登录校验通过，userId={}, username={}", user.getUserId(), user.getUsername());
        return buildLoginResponse(toAuthenticatedUser(user));
    }

    /**
     * 根据 refresh token 刷新登录结果。
     *
     * @param request 刷新令牌请求
     * @return 新的登录结果
     */
    @Override
    public UserLoginResponse refreshToken(UserRefreshTokenRequest request) {
        ParsedJwtToken parsedJwtToken = jwtTokenSupport.parseToken(request.getRefreshToken());
        if (parsedJwtToken.getTokenType() != JwtTokenType.REFRESH) {
            log.warn("刷新令牌类型非法");
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        AuthenticatedUser authenticatedUser = parsedJwtToken.getAuthenticatedUser();
        if (!refreshTokenStore.exists(authenticatedUser.getUserId(), authenticatedUser.getTokenId())) {
            log.warn("刷新令牌不存在或已失效，userId={}, tokenId={}", authenticatedUser.getUserId(), authenticatedUser.getTokenId());
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        UserDO user = userRepository.findById(authenticatedUser.getUserId())
                .orElseThrow(() -> {
                    log.warn("刷新令牌对应用户不存在，userId={}, tokenId={}", authenticatedUser.getUserId(), authenticatedUser.getTokenId());
                    return new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
                });

        // refresh token 换新时立即吊销旧 token，避免同一个 refresh token 被长期重复使用。
        refreshTokenStore.delete(authenticatedUser.getUserId(), authenticatedUser.getTokenId());
        log.info("刷新令牌校验通过并已吊销旧令牌，userId={}, tokenId={}", authenticatedUser.getUserId(), authenticatedUser.getTokenId());
        return buildLoginResponse(toAuthenticatedUser(user));
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

    /**
     * 把用户持久化对象转换成认证用户对象。
     *
     * @param user 用户持久化对象
     * @return 已认证用户
     */
    private AuthenticatedUser toAuthenticatedUser(UserDO user) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(user.getUserId());
        authenticatedUser.setUsername(user.getUsername());
        authenticatedUser.setDisplayName(user.getDisplayName());
        return authenticatedUser;
    }

    /**
     * 构造正式 JWT 登录响应。
     *
     * @param authenticatedUser 已认证用户
     * @return 登录响应
     */
    private UserLoginResponse buildLoginResponse(AuthenticatedUser authenticatedUser) {
        String accessToken = jwtTokenSupport.createAccessToken(authenticatedUser);
        String refreshToken = jwtTokenSupport.createRefreshToken(authenticatedUser);
        ParsedJwtToken parsedAccessToken = jwtTokenSupport.parseToken(accessToken);
        ParsedJwtToken parsedRefreshToken = jwtTokenSupport.parseToken(refreshToken);

        refreshTokenStore.save(
                authenticatedUser.getUserId(),
                parsedRefreshToken.getAuthenticatedUser().getTokenId(),
                parsedRefreshToken.getExpireAt()
        );
        log.info(
                "用户令牌已签发并缓存刷新令牌，userId={}, username={}, accessExpireAt={}, refreshTokenId={}, refreshExpireAt={}",
                authenticatedUser.getUserId(),
                authenticatedUser.getUsername(),
                parsedAccessToken.getExpireAt(),
                parsedRefreshToken.getAuthenticatedUser().getTokenId(),
                parsedRefreshToken.getExpireAt()
        );

        UserLoginResponse response = new UserLoginResponse();
        response.setUserId(authenticatedUser.getUserId());
        response.setUsername(authenticatedUser.getUsername());
        response.setDisplayName(authenticatedUser.getDisplayName());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType(UserSecurityConstants.TOKEN_TYPE);
        response.setAccessTokenExpireAt(parsedAccessToken.getExpireAt());
        response.setRefreshTokenExpireAt(parsedRefreshToken.getExpireAt());
        return response;
    }
}
