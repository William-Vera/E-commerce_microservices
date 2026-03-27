package com.cellc.productservice.messaging;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record OrderPaidEvent(
        Long orderId,
        Long userId,
        String paymentMethod,
        String paymentStatus,
        String orderStatus,
        Double itemsTotalAmount,
        Double discountAmount,
        Double totalAmount,
        String promotionCode,
        List<OrderPaidItem> items,
        Map<String, Object> metadata
) implements Serializable {
    public record OrderPaidItem(
            Long productId,
            Integer quantity,
            Double unitPrice,
            Double lineTotal
    ) implements Serializable {}
}
