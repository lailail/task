package com.example.ticket.payment.config;

import com.example.ticket.common.event.payment.PaymentEventConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付服务 RabbitMQ 配置。
 * 用于声明支付结果交换机、队列和 JSON 消息转换器。
 */
@Configuration
public class PaymentRabbitConfig {

    /**
     * 声明支付结果交换机。
     *
     * @return 支付结果交换机
     */
    @Bean
    public DirectExchange paymentEventExchange() {
        return new DirectExchange(PaymentEventConstants.PAYMENT_EXCHANGE, true, false);
    }

    /**
     * 声明支付结果队列。
     *
     * @return 支付结果队列
     */
    @Bean
    public Queue paymentResultQueue() {
        return new Queue(PaymentEventConstants.PAYMENT_RESULT_QUEUE, true);
    }

    /**
     * 绑定支付结果队列到交换机。
     *
     * @param paymentResultQueue 支付结果队列
     * @param paymentEventExchange 支付结果交换机
     * @return 绑定关系
     */
    @Bean
    public Binding paymentResultBinding(Queue paymentResultQueue, DirectExchange paymentEventExchange) {
        return BindingBuilder.bind(paymentResultQueue)
                .to(paymentEventExchange)
                .with(PaymentEventConstants.PAYMENT_RESULT_ROUTING_KEY);
    }

    /**
     * 声明 JSON 消息转换器。
     *
     * @return JSON 消息转换器
     */
    @Bean
    public MessageConverter paymentMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
