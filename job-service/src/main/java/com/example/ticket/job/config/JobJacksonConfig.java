package com.example.ticket.job.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 任务服务 Jackson 配置。
 * 用于为补偿事件序列化与反序列化提供统一的 `ObjectMapper`，避免时间类型在不同入口下行为不一致。
 */
@Configuration
public class JobJacksonConfig {

    /**
     * 注册任务服务使用的 ObjectMapper。
     *
     * @return 已注册 Java 时间模块的 ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
