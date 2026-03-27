package com.cellc.orderservice.messaging;

import com.cellc.orderservice.entity.OrderStatus;
import com.cellc.orderservice.entity.PaymentMethod;
import com.cellc.orderservice.entity.PaymentStatus;
import com.cellc.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderPaidPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.order-paid-routing-key}")
    private String routingKey;

    public void publishOrderPaid(Order order, Map<String, Object> metadata) {
        OrderPaidEvent event = new OrderPaidEvent(
                order.getId(),
                order.getUserId(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getStatus(),
                order.getItemsTotalAmount(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getPromotionCode(),
                order.getItems() == null ? java.util.List.of() : order.getItems().stream().map(item ->
                        new OrderPaidEvent.OrderPaidItem(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()
                        )
                ).toList(),
                metadata == null ? Map.of() : metadata
        );

        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
    }
}
