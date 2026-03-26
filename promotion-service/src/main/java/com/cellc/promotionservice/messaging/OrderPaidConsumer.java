package com.cellc.promotionservice.messaging;

import com.cellc.promotionservice.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPaidConsumer {

    private final PromotionService promotionService;

    @RabbitListener(queues = "${app.rabbitmq.order-paid-queue}")
    public void onOrderPaid(OrderPaidEvent event) {
        if (event == null) {
            return;
        }
        promotionService.recordOrderPaid(event.promotionCode(), event.orderId());
    }
}

