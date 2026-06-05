package com.example.ticket.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户服务安全基础配置。
 * 当前阶段只提供密码加密能力，供服务层通过 Spring Bean 注入，避免在业务代码中直接硬编码具体实现。
 */
@Configuration
public class UserSecurityConfig {

    /**
     * 提供密码编码器 Bean。
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
