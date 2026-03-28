package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "delivery_charges", indexes = {
        @Index(name = "idx_dc_store", columnList = "storeId"),
        @Index(name = "idx_dc_promo", columnList = "promotionId"),
        @Index(name = "idx_dc_created_at", columnList = "createdAt")
})
public class DeliveryCharge {

    @Id
    @Column(name = "charge_id", columnDefinition = "uuid")
    private UUID chargeId;

    @Column(nullable = false)
    private Long storeId;

    @Column
    private Long orderId; // nullable until order is created

    @Column
    private Double distance; // km

    @Column
    private Double weight; // kg

    @Column(length = 20)
    private String deliveryType; // standard, express, scheduled

    @Column(precision = 12, scale = 2)
    private BigDecimal baseCharge; // pre-promotion

    @Column(precision = 12, scale = 2)
    private BigDecimal totalCharge; // post-promotion

    @Column(length = 64)
    private String promotionId; // from Offers Module or evaluator id

    @Column(length = 128)
    private String promotionName;

    @Column(precision = 12, scale = 2)
    private BigDecimal cartValue; // at time of application

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (chargeId == null) chargeId = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
