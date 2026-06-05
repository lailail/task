package com.example.ticket.user.config;

import com.example.ticket.common.auth.JwtProperties;
import com.example.ticket.common.auth.JwtTokenSupport;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用户服务 JWT 配置。
 * 用于在用户服务中装配统一的 JWT 属性与签发解析组件，避免业务代码直接处理密钥细节。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class UserJwtConfig {

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
