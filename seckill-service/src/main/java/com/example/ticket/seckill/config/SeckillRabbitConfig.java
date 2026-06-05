package com.example.ticket.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 抢票服务 RabbitMQ 配置。
 * 负责把下单请求事件统一序列化为 JSON，避免生产端退回 JDK 序列化后与消费端 DTO 契约不兼容。
 */
@Configuration
public class SeckillRabbitConfig {

    /**
     * 装配 RabbitMQ JSON 消息转换器。
     *
     * @return 消息转换器
     */
    @Bean
    public MessageConverter seckillMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
