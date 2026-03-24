package com.cellc.userservice.messaging;

import com.cellc.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.user-registered-routing-key}")
    private String userRegisteredRoutingKey;

    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getNombre(),
                user.getApellido(),
                user.getEmail(),
                Instant.now()
        );
        rabbitTemplate.convertAndSend(exchangeName, userRegisteredRoutingKey, event);
    }
}
