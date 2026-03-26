package com.cellc.promotionservice.service;

import com.cellc.promotionservice.entity.Promotion;
import com.cellc.promotionservice.entity.PromotionUsage;
import com.cellc.promotionservice.repository.PromotionRepository;
import com.cellc.promotionservice.repository.PromotionUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;

    public Promotion create(String code, Double discountPercent, Boolean active) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code requerido");
        }
        if (discountPercent == null || discountPercent < 0) {
            throw new IllegalArgumentException("discountPercent inválido");
        }
        if (active == null) {
            active = true;
        }

        Promotion promo = new Promotion();
        promo.setCode(code.trim().toUpperCase());
        promo.setDiscountPercent(discountPercent);
        promo.setActive(active);
        return promotionRepository.save(promo);
    }

    public DiscountResponse validate(String code) {
        if (code == null || code.isBlank()) {
            return new DiscountResponse(null, 0.0, false);
        }

        String normalized = code.trim().toUpperCase();
        return promotionRepository.findByCode(normalized)
                .map(p -> new DiscountResponse(p.getCode(), p.getActive() ? p.getDiscountPercent() : 0.0, p.getActive()))
                .orElseGet(() -> new DiscountResponse(normalized, 0.0, false));
    }

    public void recordOrderPaid(String promotionCode, Long orderId) {
        if (promotionCode == null || promotionCode.isBlank()) {
            return;
        }

        String normalized = promotionCode.trim().toUpperCase();
        Promotion promo = promotionRepository.findByCode(normalized).orElse(null);
        if (promo == null || promo.getActive() == null || !promo.getActive()) {
            return;
        }

        PromotionUsage usage = promotionUsageRepository.findByCode(normalized).orElseGet(() -> {
            PromotionUsage u = new PromotionUsage();
            u.setCode(normalized);
            u.setTimesUsed(0L);
            return u;
        });

        usage.setTimesUsed(usage.getTimesUsed() + 1);
        usage.setLastOrderId(orderId);
        promotionUsageRepository.save(usage);
    }

    public record DiscountResponse(String code, Double discountPercent, Boolean active) {}
}

