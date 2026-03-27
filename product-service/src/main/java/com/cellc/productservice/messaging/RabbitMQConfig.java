package com.cellc.productservice.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.order-exchange:ecommerce.events}")
    private String orderExchangeName;

    @Value("${app.rabbitmq.user-registered-routing-key}")
    private String userRegisteredRoutingKey;

    @Value("${app.rabbitmq.user-registered-queue}")
    private String userRegisteredQueueName;

    @Value("${app.rabbitmq.order-paid-routing-key}")
    private String orderPaidRoutingKey;

    @Value("${app.rabbitmq.order-paid-queue}")
    private String orderPaidQueueName;

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(orderExchangeName);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(userRegisteredQueueName, true);
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(orderPaidQueueName, true);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(userExchange).with(userRegisteredRoutingKey);
    }

    @Bean
    public Binding orderPaidBinding(Queue orderPaidQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderPaidQueue).to(orderExchange).with(orderPaidRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
