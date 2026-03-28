package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Single source of truth for all platform fees.
 * Managed by adminservice; read by cartservice and orderservice via Redis cache.
 *
 * Known keys: platform_fee, packaging_fee, surge_fee
 */
@Entity
@Table(name = "fee_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeConfig {

    @Id
    @Column(name = "key", length = 50)
    private String key;

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
