package com.example.ticket.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户服务启动类。
 * 当前负责启动用户注册与登录相关的 Spring Boot 应用上下文。
 */
@SpringBootApplication(scanBasePackages = "com.example.ticket")
public class UserApplication {

    /**
     * 启动用户服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}

