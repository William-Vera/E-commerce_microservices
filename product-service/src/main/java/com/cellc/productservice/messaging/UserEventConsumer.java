package com.cellc.productservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventConsumer {

    @RabbitListener(queues = "${app.rabbitmq.user-registered-queue}")
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Evento UserRegistered recibido en product-service: userId={}, email={}",
                event.userId(), event.email());
    }
}
