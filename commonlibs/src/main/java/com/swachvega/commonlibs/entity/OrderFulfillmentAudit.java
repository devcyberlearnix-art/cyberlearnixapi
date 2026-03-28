package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "order_fulfillment_audit", indexes = {
        @Index(name = "idx_ofa_order_id", columnList = "order_id"),
        @Index(name = "idx_ofa_product_id", columnList = "product_id"),
        @Index(name = "idx_ofa_created_at", columnList = "created_at")
})
public class OrderFulfillmentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "prev_quantity")
    private Integer prevQuantity;

    @Column(name = "new_quantity")
    private Integer newQuantity;

    @Column(name = "refund_quantity")
    private Integer refundQuantity;

    @Column(name = "refund_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal refundAmount; // negative

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "changed_by", length = 100)
    private String changedBy; // user id or phone if available

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
