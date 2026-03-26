package com.cellc.orderservice.messaging;

import com.cellc.orderservice.entity.OrderStatus;
import com.cellc.orderservice.entity.PaymentMethod;
import com.cellc.orderservice.entity.PaymentStatus;

import java.io.Serializable;
import java.util.Map;

public record OrderPaidEvent(
        Long orderId,
        Long userId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        Double itemsTotalAmount,
        Double discountAmount,
        Double totalAmount,
        String promotionCode,
        Map<String, Object> metadata
) implements Serializable {}

