package com.example.ticket.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动类。
 * 当前负责启动统一入口网关应用。
 */
@SpringBootApplication
public class GatewayApplication {

    /**
     * 启动网关服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

