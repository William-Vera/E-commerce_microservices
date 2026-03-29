package com.cellc.notificationservice.messaging;

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

    @Value("${app.rabbitmq.order-paid-routing-key}")
    private String orderPaidRoutingKey;

    @Value("${app.rabbitmq.order-paid-queue}")
    private String orderPaidQueueName;

    @Bean
    public DirectExchange ecommerceExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(orderPaidQueueName, true);
    }

    @Bean
    public Binding orderPaidBinding(Queue orderPaidQueue, DirectExchange ecommerceExchange) {
        return BindingBuilder.bind(orderPaidQueue).to(ecommerceExchange).with(orderPaidRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
