package com.example.ticket.job.config;

import com.example.ticket.common.event.order.OrderEventConstants;
import com.example.ticket.common.event.stock.StockEventConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 任务服务 RabbitMQ 配置。
 * 负责声明任务编排链路依赖的交换机、队列、绑定以及消息转换器，避免库存回补和结果收敛链路因队列缺失而无法启动。
 */
@Configuration
public class JobRabbitConfig {

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
     * 声明库存释放事件直连交换机。
     *
     * @return 库存释放事件交换机
     */
    @Bean
    public DirectExchange stockReleaseExchange() {
        return new DirectExchange(StockEventConstants.STOCK_RELEASE_EXCHANGE, true, false);
    }

    /**
     * 声明订单结果消费队列。
     *
     * @return 订单结果队列
     */
    @Bean
    public Queue orderResultQueue() {
        return new Queue(OrderEventConstants.ORDER_RESULT_QUEUE, true);
    }

    /**
     * 声明库存释放消费队列。
     *
     * @return 库存释放队列
     */
    @Bean
    public Queue stockReleaseQueue() {
        return new Queue(StockEventConstants.STOCK_RELEASE_QUEUE, true);
    }

    /**
     * 将订单结果队列绑定到订单交换机。
     *
     * @param orderResultQueue 订单结果队列
     * @param orderEventExchange 订单事件交换机
     * @return 订单结果绑定关系
     */
    @Bean
    public Binding orderResultBinding(Queue orderResultQueue, DirectExchange orderEventExchange) {
        return BindingBuilder.bind(orderResultQueue)
                .to(orderEventExchange)
                .with(OrderEventConstants.ORDER_RESULT_ROUTING_KEY);
    }

    /**
     * 将库存释放队列绑定到库存释放交换机。
     *
     * @param stockReleaseQueue 库存释放队列
     * @param stockReleaseExchange 库存释放事件交换机
     * @return 库存释放绑定关系
     */
    @Bean
    public Binding stockReleaseBinding(Queue stockReleaseQueue, DirectExchange stockReleaseExchange) {
        return BindingBuilder.bind(stockReleaseQueue)
                .to(stockReleaseExchange)
                .with(StockEventConstants.STOCK_RELEASE_ROUTING_KEY);
    }

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
