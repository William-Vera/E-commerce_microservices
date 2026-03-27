package com.cellc.promotionservice.dto;

import com.cellc.promotionservice.entity.Promotion;

import java.time.LocalDate;

public record PromotionSummaryResponse(
        Long id,
        String code,
        String discountType,
        Double discountPercent,
        Double fixedAmount,
        Double minimumOrderAmount,
        Integer usageLimit,
        Long timesUsed,
        LocalDate startDate,
        LocalDate endDate,
        Boolean active,
        Boolean customerUsable
) {
    public static PromotionSummaryResponse from(Promotion promotion, Long timesUsed) {
        return new PromotionSummaryResponse(
                promotion.getId(),
                promotion.getCode(),
                promotion.getDiscountType() == null ? null : promotion.getDiscountType().name(),
                promotion.getDiscountPercent(),
                promotion.getFixedAmount(),
                promotion.getMinimumOrderAmount(),
                promotion.getUsageLimit(),
                timesUsed,
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getActive(),
                promotion.getCustomerUsable()
        );
    }
}
