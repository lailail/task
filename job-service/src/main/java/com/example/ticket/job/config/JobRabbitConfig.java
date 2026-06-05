package com.example.ticket.job.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 任务服务 RabbitMQ 配置。
 * 用于统一配置 JSON 消息转换器，保证订单结果事件能稳定反序列化。
 */
@Configuration
public class JobRabbitConfig {

    /**
     * 装配 RabbitMQ JSON 消息转换器。
     *
     * @return 消息转换器
     */
    @Bean
    public MessageConverter jobMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
