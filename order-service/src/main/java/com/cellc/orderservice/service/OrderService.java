package com.cellc.orderservice.service;

import com.cellc.orderservice.client.CartClient;
import com.cellc.orderservice.client.PromotionClient;
import com.cellc.orderservice.client.UserClient;
import com.cellc.orderservice.dto.UserContactResponse;
import com.cellc.orderservice.entity.Order;
import com.cellc.orderservice.entity.OrderItem;
import com.cellc.orderservice.entity.OrderStatus;
import com.cellc.orderservice.entity.PaymentMethod;
import com.cellc.orderservice.entity.PaymentStatus;
import com.cellc.orderservice.messaging.OrderPaidPublisher;
import com.cellc.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final double EPSILON = 0.01d;

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final PromotionClient promotionClient;
    private final OrderPaidPublisher orderPaidPublisher;
    private final UserClient userClient;

    @Transactional
    public Order checkout(Long userId, PaymentMethod paymentMethod, String promotionCode) {
        CartClient.CartResponse cart = cartClient.getCart(userId);
        validateCart(cart);

        double itemsTotal = cart.subtotal() == null ? 0.0 : cart.subtotal();
        String effectivePromotionCode = (promotionCode == null || promotionCode.isBlank()) ? cart.promotionCode() : promotionCode;
        PromotionClient.PromotionValidateResponse promotion = promotionClient.validatePromotion(userId, effectivePromotionCode, itemsTotal);
        double discountAmount = calculateDiscount(itemsTotal, promotion);
        double totalAmount = itemsTotal - discountAmount;
        if (totalAmount < 0) {
            throw new IllegalStateException("El total de la orden no puede ser negativo");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setPaymentMethod(paymentMethod);
        order.setItemsTotalAmount(itemsTotal);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setPromotionCode(effectivePromotionCode);

        cart.items().forEach(ci -> order.getItems().add(mapItem(order, ci)));

        if (paymentMethod == PaymentMethod.CASH) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.COMPLETED);
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setStatus(OrderStatus.CREATED);
        }

        Order saved = orderRepository.save(order);
        if (saved.getPaymentStatus() == PaymentStatus.PAID) {
            finalizePaidOrder(saved, userId, "cash", null);
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

        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId es obligatorio");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return order;
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.COMPLETED);
        Order saved = orderRepository.save(order);
        finalizePaidOrder(saved, userId, "online", transactionId.trim());
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getByUser(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order no encontrada: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permisos para ver este pedido");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> listByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public boolean userHasOrders(Long userId) {
        return orderRepository.existsByUserId(userId);
    }

    private void finalizePaidOrder(Order order, Long userId, String flow, String transactionId) {
        cartClient.clearCart(userId);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("flow", flow);
        metadata.put("paidAt", order.getCreatedAt().toString());
        if (transactionId != null && !transactionId.isBlank()) {
            metadata.put("transactionId", transactionId);
        }
        try {
            UserContactResponse userContact = userClient.getUserContact(userId);
            orderPaidPublisher.publishOrderPaid(order, userContact, metadata);
        } catch (Exception ex) {
            log.warn("No se pudo enriquecer el evento de la orden {} con datos del usuario: {}", order.getId(), ex.getMessage());
            orderPaidPublisher.publishOrderPaid(order, null, metadata);
        }
    }

    private void validateCart(CartClient.CartResponse cart) {
        if (cart == null) {
            throw new IllegalArgumentException("No se encontro el carrito del usuario");
        }
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("El carrito esta vacio");
        }
        if (cart.subtotal() == null || cart.subtotal() < 0) {
            throw new IllegalArgumentException("El subtotal del carrito no es valido");
        }

        double recalculatedSubtotal = 0.0;
        for (CartClient.CartItemDto item : cart.items()) {
            recalculatedSubtotal += validateCartItem(item);
        }

        if (Math.abs(recalculatedSubtotal - cart.subtotal()) > EPSILON) {
            throw new IllegalStateException("El subtotal del carrito no coincide con la suma de los items");
        }
    }

    private double validateCartItem(CartClient.CartItemDto item) {
        if (item.productId() == null) {
            throw new IllegalArgumentException("Cada item debe tener productId");
        }
        if (item.quantity() == null || item.quantity() <= 0) {
            throw new IllegalArgumentException("La cantidad de cada item debe ser mayor que cero");
        }
        if (item.unitPrice() == null || item.unitPrice() < 0) {
            throw new IllegalArgumentException("El precio unitario de cada item debe ser valido");
        }
        if (item.lineTotal() == null || item.lineTotal() < 0) {
            throw new IllegalArgumentException("El total de linea de cada item debe ser valido");
        }

        double expectedLineTotal = item.quantity() * item.unitPrice();
        if (Math.abs(expectedLineTotal - item.lineTotal()) > EPSILON) {
            throw new IllegalStateException("El total de linea del producto " + item.productId() + " no coincide con cantidad por precio");
        }

        return item.lineTotal();
    }

    private OrderItem mapItem(Order order, CartClient.CartItemDto cartItem) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(cartItem.productId());
        item.setQuantity(cartItem.quantity());
        item.setUnitPrice(cartItem.unitPrice());
        item.setLineTotal(cartItem.lineTotal());
        return item;
    }

    private double calculateDiscount(double itemsTotal, PromotionClient.PromotionValidateResponse promotion) {
        if (promotion == null || promotion.active() == null || !promotion.active()) {
            return 0.0;
        }
        if ("FIXED_AMOUNT".equalsIgnoreCase(promotion.discountType())) {
            double fixedAmount = promotion.fixedAmount() == null ? 0.0 : promotion.fixedAmount();
            return Math.min(itemsTotal, fixedAmount);
        }
        double discountPercent = promotion.discountPercent() == null ? 0.0 : promotion.discountPercent();
        return Math.min(itemsTotal, itemsTotal * discountPercent / 100.0);
    }
}
