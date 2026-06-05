package com.example.ticket.gateway.filter;

import com.example.ticket.common.auth.AuthHeaderConstants;
import com.example.ticket.common.auth.JwtProperties;
import com.example.ticket.common.auth.JwtTokenSupport;
import com.example.ticket.gateway.config.GatewayAuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 网关统一认证过滤器测试。
 * 用于验证白名单放行、未授权拦截和认证头透传行为。
 */
class GatewayAuthenticationFilterTest {

    /**
     * 白名单路径应直接放行。
     */
    @Test
    void should_allow_permit_path_without_token() {
        AtomicReference<String> forwardedPath = new AtomicReference<>();
        GatewayAuthenticationFilter filter = buildFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/users/login").build()
        );

        filter.filter(exchange, captureChain(forwardedPath)).block();

        assertEquals("/api/v1/users/login", forwardedPath.get());
    }

    /**
     * 缺少 token 时应返回未授权。
     */
    @Test
    void should_reject_request_when_token_is_missing() {
        GatewayAuthenticationFilter filter = buildFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/seckill/reservations").build()
        );

        filter.filter(exchange, captureChain(new AtomicReference<>())).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    /**
     * access token 合法时应向下游透传统一身份头。
     */
    @Test
    void should_forward_authenticated_headers_when_access_token_is_valid() {
        AtomicReference<String> forwardedUserId = new AtomicReference<>();
        AtomicReference<String> forwardedUsername = new AtomicReference<>();
        GatewayAuthenticationFilter filter = buildFilter();
        String accessToken = buildJwtTokenSupport().createAccessToken(buildAuthenticatedUser());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/seckill/reservations")
                        .header(AuthHeaderConstants.AUTHORIZATION, AuthHeaderConstants.BEARER_PREFIX + accessToken)
                        .build()
        );

        filter.filter(exchange, currentExchange -> {
            forwardedUserId.set(currentExchange.getRequest().getHeaders().getFirst(AuthHeaderConstants.AUTHENTICATED_USER_ID));
            forwardedUsername.set(currentExchange.getRequest().getHeaders().getFirst(AuthHeaderConstants.AUTHENTICATED_USERNAME));
            return Mono.empty();
        }).block();

        assertEquals("1", forwardedUserId.get());
        assertEquals("alice", forwardedUsername.get());
    }

    /**
     * 构造测试过滤器。
     *
     * @return 过滤器
     */
    private GatewayAuthenticationFilter buildFilter() {
        GatewayAuthProperties gatewayAuthProperties = new GatewayAuthProperties();
        gatewayAuthProperties.setPermitPaths(List.of(
                "/api/v1/users/register",
                "/api/v1/users/login",
                "/api/v1/users/token/refresh",
                "/api/v1/activities/**",
                "/actuator/**"
        ));
        return new GatewayAuthenticationFilter(buildJwtTokenSupport(), gatewayAuthProperties, new ObjectMapper());
    }

    /**
     * 构造测试用 JWT 支持组件。
     *
     * @return JWT 支持组件
     */
    private JwtTokenSupport buildJwtTokenSupport() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("ticket-system-test");
        jwtProperties.setSecret("ticket-local-demo-jwt-secret-key-please-change");
        jwtProperties.setAccessTokenExpireSeconds(1800);
        jwtProperties.setRefreshTokenExpireSeconds(604800);
        return new JwtTokenSupport(jwtProperties);
    }

    /**
     * 构造测试用认证用户。
     *
     * @return 认证用户
     */
    private com.example.ticket.common.auth.AuthenticatedUser buildAuthenticatedUser() {
        com.example.ticket.common.auth.AuthenticatedUser authenticatedUser = new com.example.ticket.common.auth.AuthenticatedUser();
        authenticatedUser.setUserId(1L);
        authenticatedUser.setUsername("alice");
        authenticatedUser.setDisplayName("Alice");
        return authenticatedUser;
    }

    /**
     * 构造用于观察是否继续放行的过滤链。
     *
     * @param forwardedPath 透传路径记录器
     * @return 过滤链
     */
    private GatewayFilterChain captureChain(AtomicReference<String> forwardedPath) {
        return exchange -> {
            forwardedPath.set(exchange.getRequest().getPath().value());
            assertNotNull(exchange.getRequest());
            return Mono.empty();
        };
    }
}
