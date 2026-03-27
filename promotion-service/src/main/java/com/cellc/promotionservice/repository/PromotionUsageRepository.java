package com.cellc.promotionservice.repository;

import com.cellc.promotionservice.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {
    long countByCode(String code);
    boolean existsByCodeAndUserId(String code, Long userId);
    boolean existsByOrderId(Long orderId);
}
