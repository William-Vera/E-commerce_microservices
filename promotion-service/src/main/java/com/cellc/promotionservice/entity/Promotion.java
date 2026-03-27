package com.cellc.promotionservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Getter
@Setter
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiscountType discountType;

    private Double discountPercent;

    private Double fixedAmount;

    private Double minimumOrderAmount;

    private Integer usageLimit;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Boolean customerUsable;
}
