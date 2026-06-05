package com.example.ticket.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 定时任务服务启动类。
 * 当前负责启动后续超时关闭、补偿与回查所在的应用上下文。
 */
@SpringBootApplication
public class JobApplication {

    /**
     * 启动定时任务服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }
}

