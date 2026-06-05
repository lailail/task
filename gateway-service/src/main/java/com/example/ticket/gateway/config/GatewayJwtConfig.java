package com.example.ticket.gateway.config;

import com.example.ticket.common.auth.JwtProperties;
import com.example.ticket.common.auth.JwtTokenSupport;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关 JWT 配置。
 * 用于在网关服务中装配统一的 JWT 配置属性和验签支持组件。
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, GatewayAuthProperties.class})
public class GatewayJwtConfig {

    /**
     * 提供 JWT 支持组件。
     *
     * @param jwtProperties JWT 配置属性
     * @return JWT 支持组件
     */
    @Bean
    public JwtTokenSupport jwtTokenSupport(JwtProperties jwtProperties) {
        return new JwtTokenSupport(jwtProperties);
    }
}
