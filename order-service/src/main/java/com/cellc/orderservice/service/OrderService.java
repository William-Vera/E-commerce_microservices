package com.cellc.orderservice.service;

import com.cellc.orderservice.client.CartClient;
import com.cellc.orderservice.client.PromotionClient;
import com.cellc.orderservice.entity.*;
import com.cellc.orderservice.messaging.OrderPaidPublisher;
import com.cellc.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final PromotionClient promotionClient;
    private final OrderPaidPublisher orderPaidPublisher;

    @Transactional
    public Order checkout(Long userId, PaymentMethod paymentMethod, String promotionCode) {
        CartClient.CartResponse cart = cartClient.getCart(userId);
        if (cart == null) {
            throw new IllegalArgumentException("No se encontró el carrito del usuario");
        }
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        double itemsTotal = cart.total() == null ? 0.0 : cart.total();
        double discountPercent = promotionClient.getDiscountPercentOrZero(userId, promotionCode);
        double discountAmount = itemsTotal * discountPercent / 100.0;
        double totalAmount = itemsTotal - discountAmount;

        Order order = new Order();
        order.setUserId(userId);
        order.setPaymentMethod(paymentMethod);
        order.setItemsTotalAmount(itemsTotal);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setPromotionCode(promotionCode);

        cart.items().forEach(ci -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(ci.productId());
            oi.setQuantity(ci.quantity());
            oi.setUnitPrice(ci.unitPrice());
            oi.setLineTotal(ci.lineTotal());
            order.getItems().add(oi);
        });

        if (paymentMethod == PaymentMethod.CASH) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.COMPLETED);
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setStatus(OrderStatus.CREATED);
        }

        Order saved = orderRepository.save(order);
        if (saved.getPaymentStatus() == PaymentStatus.PAID) {
            cartClient.clearCart(userId);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("flow", "cash");
            orderPaidPublisher.publishOrderPaid(saved, metadata);
        }

        return saved;
    }

    @Transactional
    public Order confirmOnline(Long userId, Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order no encontrada: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permisos para confirmar este pedido");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return order;
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.COMPLETED);
        Order saved = orderRepository.save(order);

        cartClient.clearCart(userId);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("flow", "online");
        metadata.put("transactionId", transactionId);
        orderPaidPublisher.publishOrderPaid(saved, metadata);
        return saved;
    }

    @Transactional
    public Order getByUser(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order no encontrada: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permisos para ver este pedido");
        }
        return order;
    }
}

