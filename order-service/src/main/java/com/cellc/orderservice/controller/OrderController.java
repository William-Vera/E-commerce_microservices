package com.cellc.orderservice.controller;

import com.cellc.orderservice.entity.*;
import com.cellc.orderservice.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping("/checkout")
    public OrderResponse checkout(
            @RequestHeader(name = "X-User-Id") Long userId,
            @Valid @RequestBody CheckoutRequest request
    ) {
        PaymentMethod method = PaymentMethod.valueOf(request.paymentMethod().toUpperCase());
        Order order = service.checkout(userId, method, request.promotionCode());
        return OrderResponse.from(order);
    }

    @PostMapping("/{orderId}/confirm-online")
    public OrderResponse confirmOnline(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody ConfirmOnlineRequest request
    ) {
        Order order = service.confirmOnline(userId, orderId, request.transactionId());
        return OrderResponse.from(order);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable Long orderId
    ) {
        return OrderResponse.from(service.getByUser(userId, orderId));
    }

    @GetMapping({"", "/"})
    public List<OrderResponse> listOrders(
            @RequestHeader(name = "X-User-Id") Long userId
    ) {
        return service.listByUser(userId).stream().map(OrderResponse::from).toList();
    }

    public record CheckoutRequest(
            @NotBlank String paymentMethod,
            String promotionCode
    ) {}

    public record ConfirmOnlineRequest(
            @NotBlank String transactionId
    ) {}

    public record OrderItemResponse(
            Long productId,
            Integer quantity,
            Double unitPrice,
            Double lineTotal
    ) {}

    public record OrderResponse(
            Long orderId,
            java.time.Instant createdAt,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            OrderStatus status,
            Double itemsTotalAmount,
            Double discountAmount,
            Double totalAmount,
            String promotionCode,
            List<OrderItemResponse> items
    ) {
        public static OrderResponse from(Order order) {
            List<OrderItemResponse> items = order.getItems() == null
                    ? List.of()
                    : order.getItems().stream().map(oi ->
                    new OrderItemResponse(
                            oi.getProductId(),
                            oi.getQuantity(),
                            oi.getUnitPrice(),
                            oi.getLineTotal()
                    )
            ).toList();

            return new OrderResponse(
                    order.getId(),
                    order.getCreatedAt(),
                    order.getPaymentMethod(),
                    order.getPaymentStatus(),
                    order.getStatus(),
                    order.getItemsTotalAmount(),
                    order.getDiscountAmount(),
                    order.getTotalAmount(),
                    order.getPromotionCode(),
                    items
            );
        }
    }
}
