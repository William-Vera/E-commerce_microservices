package com.cellc.promotionservice.messaging;

import java.io.Serializable;
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
        Map<String, Object> metadata
) implements Serializable {}

