package com.cellc.promotionservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_usage", indexes = {
        @Index(name = "idx_promo_usage_code", columnList = "code"),
        @Index(name = "idx_promo_usage_user", columnList = "userId")
})
@Getter
@Setter
public class PromotionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private LocalDateTime usedAt;
}
