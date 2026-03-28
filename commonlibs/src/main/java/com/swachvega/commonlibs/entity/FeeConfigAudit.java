package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Immutable audit trail — one row per fee change.
 * Never deleted; provides full history for compliance and debugging.
 */
@Entity
@Table(name = "fee_config_audit", indexes = {
        @Index(name = "idx_fca_key",        columnList = "key"),
        @Index(name = "idx_fca_changed_at", columnList = "changed_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeConfigAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key", length = 50, nullable = false)
    private String key;

    @Column(name = "old_value", precision = 10, scale = 2)
    private BigDecimal oldValue;

    @Column(name = "new_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal newValue;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;
}
