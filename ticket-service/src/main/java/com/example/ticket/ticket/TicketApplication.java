package com.example.ticket.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 活动查询服务启动类。
 * 当前负责启动活动与票种查询相关的 Spring Boot 应用上下文。
 */
@SpringBootApplication(scanBasePackages = "com.example.ticket")
public class TicketApplication {

    /**
     * 启动活动查询服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TicketApplication.class, args);
    }
}

