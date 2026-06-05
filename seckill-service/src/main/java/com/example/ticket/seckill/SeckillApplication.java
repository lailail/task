package com.example.ticket.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 抢票服务启动类。
 * 当前负责启动后续秒杀链路所在的应用上下文。
 */
@SpringBootApplication(scanBasePackages = "com.example.ticket")
public class SeckillApplication {

    /**
     * 启动抢票服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }
}

