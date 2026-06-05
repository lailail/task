package com.example.ticket.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 支付服务启动类。
 * 用于启动模拟支付域、支付结果发布和对账回查相关组件。
 */
@SpringBootApplication(scanBasePackages = "com.example.ticket")
@EnableScheduling
public class PaymentApplication {

    /**
     * 启动支付服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
