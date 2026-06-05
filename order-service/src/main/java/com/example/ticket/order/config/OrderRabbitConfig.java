package com.example.ticket.order.config;

import com.example.ticket.common.event.order.OrderEventConstants;
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
 * 订单服务 RabbitMQ 配置。
 * 负责声明下单链路所需的交换机、队列、绑定以及消息转换器，避免服务启动依赖外部手工建队列。
 */
@Configuration
public class OrderRabbitConfig {

    /**
     * 声明订单事件直连交换机。
     *
     * @return 订单事件交换机
     */
    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange(OrderEventConstants.ORDER_CREATE_EXCHANGE, true, false);
    }

    /**
     * 声明下单请求消费队列。
     *
     * @return 下单请求队列
     */
    @Bean
    public Queue orderCreateQueue() {
        return new Queue(OrderEventConstants.ORDER_CREATE_QUEUE, true);
    }

    /**
     * 将下单请求队列绑定到订单交换机。
     *
     * @param orderCreateQueue 下单请求队列
     * @param orderEventExchange 订单事件交换机
     * @return 下单请求绑定关系
     */
    @Bean
    public Binding orderCreateBinding(Queue orderCreateQueue, DirectExchange orderEventExchange) {
        return BindingBuilder.bind(orderCreateQueue)
                .to(orderEventExchange)
                .with(OrderEventConstants.ORDER_CREATE_ROUTING_KEY);
    }

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
     * 声明支付结果消费队列。
     *
     * @return 支付结果队列
     */
    @Bean
    public Queue paymentResultQueue() {
        return new Queue(PaymentEventConstants.PAYMENT_RESULT_QUEUE, true);
    }

    /**
     * 把支付结果队列绑定到支付交换机。
     *
     * @param paymentResultQueue 支付结果队列
     * @param paymentEventExchange 支付结果交换机
     * @return 支付结果绑定关系
     */
    @Bean
    public Binding paymentResultBinding(Queue paymentResultQueue, DirectExchange paymentEventExchange) {
        return BindingBuilder.bind(paymentResultQueue)
                .to(paymentEventExchange)
                .with(PaymentEventConstants.PAYMENT_RESULT_ROUTING_KEY);
    }

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
