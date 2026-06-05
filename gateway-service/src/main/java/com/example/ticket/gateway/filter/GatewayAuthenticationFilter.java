package com.example.ticket.gateway.filter;

import com.example.ticket.common.auth.AuthHeaderConstants;
import com.example.ticket.common.auth.AuthenticatedUser;
import com.example.ticket.common.auth.JwtTokenSupport;
import com.example.ticket.common.auth.JwtTokenType;
import com.example.ticket.common.auth.ParsedJwtToken;
import com.example.ticket.common.error.BusinessException;
import com.example.ticket.common.error.ErrorCode;
import com.example.ticket.common.response.ApiResponse;
import com.example.ticket.gateway.config.GatewayAuthProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关统一认证过滤器。
 * 用于在统一入口完成 Bearer Token 验签、清理伪造身份头并把认证结果透传给下游服务。
 */
@Component
public class GatewayAuthenticationFilter implements GlobalFilter, Ordered {
    private final JwtTokenSupport jwtTokenSupport;
    private final GatewayAuthProperties gatewayAuthProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 构造网关统一认证过滤器。
     *
     * @param jwtTokenSupport JWT 支持组件
     * @param gatewayAuthProperties 网关认证配置
     * @param objectMapper JSON 对象映射器
     */
    public GatewayAuthenticationFilter(
            JwtTokenSupport jwtTokenSupport,
            GatewayAuthProperties gatewayAuthProperties,
            ObjectMapper objectMapper
    ) {
        this.jwtTokenSupport = jwtTokenSupport;
        this.gatewayAuthProperties = gatewayAuthProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行统一认证过滤。
     *
     * @param exchange 当前请求交换对象
     * @param chain 网关过滤链
     * @return 过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isPermitPath(path)) {
            return chain.filter(exchange);
        }

        try {
            String authorization = exchange.getRequest().getHeaders().getFirst(AuthHeaderConstants.AUTHORIZATION);
            if (authorization == null || !authorization.startsWith(AuthHeaderConstants.BEARER_PREFIX)) {
                throw new BusinessException(ErrorCode.TOKEN_INVALID);
            }

            ParsedJwtToken parsedJwtToken = jwtTokenSupport.parseToken(
                    authorization.substring(AuthHeaderConstants.BEARER_PREFIX.length()).trim()
            );
            if (parsedJwtToken.getTokenType() != JwtTokenType.ACCESS) {
                throw new BusinessException(ErrorCode.TOKEN_INVALID);
            }

            ServerHttpRequest mutatedRequest = mutateRequest(exchange.getRequest(), parsedJwtToken.getAuthenticatedUser());
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (BusinessException exception) {
            return writeUnauthorizedResponse(exchange, exception.getCode() == ErrorCode.TOKEN_EXPIRED.getCode()
                    ? ErrorCode.TOKEN_EXPIRED
                    : ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 获取过滤器顺序。
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 判断请求路径是否允许匿名访问。
     *
     * @param path 请求路径
     * @return 是否允许匿名访问
     */
    private boolean isPermitPath(String path) {
        return gatewayAuthProperties.getPermitPaths().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    /**
     * 构造携带认证身份头的新请求。
     *
     * @param request 原始请求
     * @param authenticatedUser 已认证用户
     * @return 新请求
     */
    private ServerHttpRequest mutateRequest(ServerHttpRequest request, AuthenticatedUser authenticatedUser) {
        return request.mutate()
                .headers(headers -> {
                    // 任何来自外部的身份头都必须先清理，再由网关重新写入，避免客户端伪造。
                    headers.remove(AuthHeaderConstants.AUTHENTICATED_USER_ID);
                    headers.remove(AuthHeaderConstants.AUTHENTICATED_USERNAME);
                    headers.remove(AuthHeaderConstants.AUTHENTICATED_DISPLAY_NAME);
                    headers.remove(AuthHeaderConstants.AUTHENTICATED_TOKEN_ID);
                    headers.set(AuthHeaderConstants.AUTHENTICATED_USER_ID, String.valueOf(authenticatedUser.getUserId()));
                    if (authenticatedUser.getUsername() != null) {
                        headers.set(AuthHeaderConstants.AUTHENTICATED_USERNAME, authenticatedUser.getUsername());
                    }
                    if (authenticatedUser.getDisplayName() != null) {
                        headers.set(AuthHeaderConstants.AUTHENTICATED_DISPLAY_NAME, authenticatedUser.getDisplayName());
                    }
                    if (authenticatedUser.getTokenId() != null) {
                        headers.set(AuthHeaderConstants.AUTHENTICATED_TOKEN_ID, authenticatedUser.getTokenId());
                    }
                })
                .build();
    }

    /**
     * 向调用方返回统一未授权响应。
     *
     * @param exchange 当前请求交换对象
     * @param errorCode 错误码
     * @return 响应结果
     */
    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, ErrorCode errorCode) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] payload = toResponseBytes(ApiResponse.failure(errorCode));
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(payload)));
    }

    /**
     * 把统一响应对象转换成 JSON 字节数组。
     *
     * @param response 统一响应对象
     * @return JSON 字节数组
     */
    private byte[] toResponseBytes(ApiResponse<Void> response) {
        try {
            return objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException exception) {
            return "{\"code\":500,\"message\":\"system error\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
