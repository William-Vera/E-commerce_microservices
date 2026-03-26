package com.cellc.promotionservice.repository;

import com.cellc.promotionservice.entity.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {
    Optional<PromotionUsage> findByCode(String code);
}

