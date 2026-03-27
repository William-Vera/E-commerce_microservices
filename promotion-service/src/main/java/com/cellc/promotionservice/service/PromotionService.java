package com.cellc.promotionservice.service;

import com.cellc.promotionservice.controller.PromotionController;
import com.cellc.promotionservice.dto.PromotionSummaryResponse;
import com.cellc.promotionservice.entity.DiscountType;
import com.cellc.promotionservice.entity.Promotion;
import com.cellc.promotionservice.entity.PromotionUsage;
import com.cellc.promotionservice.repository.PromotionRepository;
import com.cellc.promotionservice.repository.PromotionUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;

    public Promotion create(PromotionController.CreatePromotionRequest request) {
        validateCreateRequest(request);

        DiscountType discountType = resolveDiscountType(request);
        Boolean active = request.active() == null ? true : request.active();
        Boolean customerUsable = request.customerUsable() == null ? true : request.customerUsable();
        Double minimumOrderAmount = request.minimumOrderAmount() == null ? 0.0 : request.minimumOrderAmount();

        Promotion promo = new Promotion();
        promo.setCode(request.code().trim().toUpperCase());
        promo.setDiscountType(discountType);
        promo.setDiscountPercent(discountType == DiscountType.PERCENTAGE ? request.discountPercent() : null);
        promo.setFixedAmount(discountType == DiscountType.FIXED_AMOUNT ? request.fixedAmount() : null);
        promo.setMinimumOrderAmount(minimumOrderAmount);
        promo.setUsageLimit(request.usageLimit());
        promo.setStartDate(request.startDate());
        promo.setEndDate(request.endDate());
        promo.setActive(active);
        promo.setCustomerUsable(customerUsable);
        return promotionRepository.save(promo);
    }

    public List<PromotionSummaryResponse> listAll() {
        return promotionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Promotion::getId).reversed())
                .map(promotion -> PromotionSummaryResponse.from(
                        promotion,
                        promotionUsageRepository.countByCode(promotion.getCode())
                ))
                .toList();
    }

    public DiscountResponse validate(String code, Long userId, Double orderAmount) {
        if (code == null || code.isBlank()) {
            return DiscountResponse.inactive(null);
        }

        String normalized = code.trim().toUpperCase();
        return promotionRepository.findByCode(normalized)
                .map(p -> buildDiscountResponse(p, userId, orderAmount))
                .orElseGet(() -> DiscountResponse.inactive(normalized));
    }

    public void recordOrderPaid(String promotionCode, Long userId, Long orderId) {
        if (promotionCode == null || promotionCode.isBlank()) {
            return;
        }
        if (userId == null || orderId == null || promotionUsageRepository.existsByOrderId(orderId)) {
            return;
        }

        String normalized = promotionCode.trim().toUpperCase();
        Promotion promo = promotionRepository.findByCode(normalized).orElse(null);
        if (promo == null || promo.getActive() == null || !promo.getActive()) {
            return;
        }

        PromotionUsage usage = new PromotionUsage();
        usage.setCode(normalized);
        usage.setUserId(userId);
        usage.setOrderId(orderId);
        usage.setUsedAt(LocalDateTime.now());
        promotionUsageRepository.save(usage);
    }

    private DiscountResponse buildDiscountResponse(Promotion promotion, Long userId, Double orderAmount) {
        if (!isPromotionUsable(promotion, userId, orderAmount)) {
            return DiscountResponse.inactive(promotion.getCode());
        }

        Long timesUsed = promotionUsageRepository.countByCode(promotion.getCode());
        boolean usedByUser = userId != null && promotionUsageRepository.existsByCodeAndUserId(promotion.getCode(), userId);

        return new DiscountResponse(
                promotion.getCode(),
                promotion.getDiscountType(),
                promotion.getDiscountPercent(),
                promotion.getFixedAmount(),
                promotion.getMinimumOrderAmount(),
                promotion.getUsageLimit(),
                timesUsed,
                usedByUser,
                true
        );
    }

    private boolean isPromotionUsable(Promotion promotion, Long userId, Double orderAmount) {
        if (promotion.getActive() == null || !promotion.getActive()) {
            return false;
        }
        if (promotion.getCustomerUsable() == null || !promotion.getCustomerUsable()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        if (promotion.getStartDate() != null && today.isBefore(promotion.getStartDate())) {
            return false;
        }
        if (promotion.getEndDate() != null && today.isAfter(promotion.getEndDate())) {
            return false;
        }
        if (promotion.getMinimumOrderAmount() != null && orderAmount != null
                && orderAmount < promotion.getMinimumOrderAmount()) {
            return false;
        }
        if (userId != null && promotionUsageRepository.existsByCodeAndUserId(promotion.getCode(), userId)) {
            return false;
        }
        if (promotion.getUsageLimit() != null) {
            long timesUsed = promotionUsageRepository.countByCode(promotion.getCode());
            if (timesUsed >= promotion.getUsageLimit()) {
                return false;
            }
        }
        return true;
    }

    private void validateCreateRequest(PromotionController.CreatePromotionRequest request) {
        if (request.code() == null || request.code().isBlank()) {
            throw new IllegalArgumentException("code requerido");
        }

        DiscountType discountType = resolveDiscountType(request);
        if (discountType == DiscountType.PERCENTAGE) {
            if (request.discountPercent() == null || request.discountPercent() <= 0) {
                throw new IllegalArgumentException("discountPercent invalido");
            }
        } else if (request.fixedAmount() == null || request.fixedAmount() <= 0) {
            throw new IllegalArgumentException("fixedAmount invalido");
        }

        if (request.startDate() != null && request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("endDate no puede ser menor que startDate");
        }
    }

    private DiscountType resolveDiscountType(PromotionController.CreatePromotionRequest request) {
        if (request.discountType() != null) {
            return request.discountType();
        }
        if (request.fixedAmount() != null && request.fixedAmount() > 0) {
            return DiscountType.FIXED_AMOUNT;
        }
        return DiscountType.PERCENTAGE;
    }

    public record DiscountResponse(
            String code,
            DiscountType discountType,
            Double discountPercent,
            Double fixedAmount,
            Double minimumOrderAmount,
            Integer usageLimit,
            Long timesUsed,
            Boolean usedByUser,
            Boolean active
    ) {
        public static DiscountResponse inactive(String code) {
            return new DiscountResponse(code, null, 0.0, 0.0, null, null, 0L, false, false);
        }
    }
}
