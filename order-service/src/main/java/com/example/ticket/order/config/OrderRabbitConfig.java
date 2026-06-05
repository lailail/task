package com.example.ticket.order.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单服务 RabbitMQ 配置。
 * 用于统一配置 JSON 消息转换器，保证跨服务事件契约按显式字段结构传递。
 */
@Configuration
public class OrderRabbitConfig {

    /**
     * 装配 RabbitMQ JSON 消息转换器。
     *
     * @return 消息转换器
     */
    @Bean
    public MessageConverter orderMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
