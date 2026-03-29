package com.cellc.notificationservice.messaging;

import com.cellc.notificationservice.service.SaleEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidConsumer {

    private final SaleEmailService saleEmailService;

    @RabbitListener(queues = "${app.rabbitmq.order-paid-queue}")
    public void onOrderPaid(OrderPaidEvent event) {
        if (event == null || event.orderId() == null) {
            return;
        }
        log.info("Evento order.paid recibido para orderId={}", event.orderId());
        saleEmailService.sendSaleDetails(event);
    }
}
