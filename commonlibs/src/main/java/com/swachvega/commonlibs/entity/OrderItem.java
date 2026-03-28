package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_item_order_id", columnList = "orderId"),
        @Index(name = "idx_order_item_product_id", columnList = "productId")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 200)
    private String productName; // Snapshot of product name at time of order

    @Column(length = 500)
    private String productDescription; // Snapshot of product description

    @Column(length = 255)
    private String productImageUrl; // Snapshot of product image

    @Column(nullable = false)
    private Integer quantity = 1;

    // Persist initial ordered quantity for downstream dashboards
    @Column(name = "original_quantity")
    private Integer originalQuantity;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal unitPrice = BigDecimal.ZERO; // Price per unit at time of order

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalPrice = BigDecimal.ZERO; // quantity * unitPrice

    // Snapshot of product MRP at time of order for accurate savings calculation
    @Column(name = "mrp", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal mrp;

    // Optional original GST total at time of order (filled by orderservice if available)
    @Column(name = "original_total_gst_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal originalTotalGstAmount;

    @Column(nullable = false)
    private Double unitWeight = 0.0; // Weight per unit in kg

    @Column(nullable = false)
    private Double totalWeight = 0.0; // quantity * unitWeight

    @Column(length = 50)
    private String unit; // kg, g, piece, etc.

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String category;

    @Column(length = 500)
    private String specialInstructions;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Many-to-one relationship with Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable = false, updatable = false)
    private Order order;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    @PrePersist
    public void prePersist() {
        calculateTotals();
        if (this.originalQuantity == null) {
            this.originalQuantity = this.quantity;
        }
    }

    private void calculateTotals() {
        BigDecimal qty = BigDecimal.valueOf(this.quantity != null ? this.quantity : 0);
        BigDecimal price = this.unitPrice != null ? this.unitPrice : BigDecimal.ZERO;
        this.totalPrice = price.multiply(qty).setScale(2, RoundingMode.HALF_UP);
        this.totalWeight = this.quantity * this.unitWeight;
    }

    // Business logic methods
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        calculateTotals();
    }

    public void updateUnitPrice(Double newUnitPrice) {
        if (newUnitPrice == null || newUnitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.unitPrice = BigDecimal.valueOf(newUnitPrice);
        calculateTotals();
    }

    // Refund tracking fields
    @Column(name = "refund_quantity")
    private Integer refundQuantity; // ordered - fulfilled

    @Column(name = "refund_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal refundAmount; // negative amount

    // GST component of refund for this item (negative value)
    @Column(name = "refund_gst_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal refundGstAmount;

    @Column(name = "refund_reason", length = 255)
    private String refundReason;

    @Column(name = "fulfillment_status", length = 30)
    private String fulfillmentStatus;
}
