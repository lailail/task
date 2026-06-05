package com.example.ticket.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单服务启动类。
 * 当前负责启动后续异步下单链路所在的应用上下文。
 */
@SpringBootApplication(scanBasePackages = "com.example.ticket")
public class OrderApplication {

    /**
     * 启动订单服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}

