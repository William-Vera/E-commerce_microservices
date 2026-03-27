package com.cellc.productservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_orders")
@Getter
@Setter
public class ProcessedOrder {

    @Id
    private Long orderId;

    @Column(nullable = false)
    private LocalDateTime processedAt;
}
