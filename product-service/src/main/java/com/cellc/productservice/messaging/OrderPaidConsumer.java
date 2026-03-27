package com.cellc.productservice.messaging;

import com.cellc.productservice.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidConsumer {

    private final ProductoService productoService;

    @RabbitListener(queues = "${app.rabbitmq.order-paid-queue}")
    public void onOrderPaid(OrderPaidEvent event) {
        if (event == null || event.items() == null || event.items().isEmpty()) {
            return;
        }

        productoService.discountStock(event.orderId(), event.items());
        log.info("Stock actualizado por order.paid: orderId={}", event.orderId());
    }
}
