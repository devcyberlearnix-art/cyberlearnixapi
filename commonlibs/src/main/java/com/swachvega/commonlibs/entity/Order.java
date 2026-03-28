package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user_id", columnList = "user_id"),
        @Index(name = "idx_order_store_id", columnList = "store_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_date", columnList = "order_date"),
        @Index(name = "idx_orders_vertical", columnList = "store_vertical")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long cartId; // Reference to the cart that was converted to this order

    @Column(nullable = false, unique = true, length = 20)
    private String orderNumber; // Human-readable order number (e.g., ORD-2025-001234)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private OrderStatus status = OrderStatus.PENDING; // grocery default; restaurant service sets PLACED explicitly

    /**
     * Vertical this order belongs to. Denormalized from stores.store_vertical
     * for efficient per-vertical queries without joins.
     */
    @Column(name = "store_vertical", length = 20)
    private String storeVertical = "GROCERY"; // "GROCERY" | "RESTAURANT"

    @Column(name = "overall_fulfillment_status", length = 32)
    private String overallFulfillmentStatus;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "preparation_time_minutes")
    private Integer preparationTimeMinutes;

    @Column(nullable = false)
    private Integer totalItems = 0;

    @Column(nullable = false)
    private Double totalWeight = 0.0; // Total weight in kg

    @Column(nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column
    private LocalDateTime confirmedAt;

    @Column(name = "preparing_at")
    private LocalDateTime preparingAt;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "driver_assigned_at")
    private LocalDateTime driverAssignedAt;

    @Column
    private LocalDateTime packedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column
    private LocalDateTime shippedAt;

    @Column
    private LocalDateTime deliveredAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(length = 500)
    private String specialInstructions;

    @Column(length = 500)
    private String cancellationReason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getOverallFulfillmentStatus() {
        return overallFulfillmentStatus;
    }

    public void setOverallFulfillmentStatus(String overallFulfillmentStatus) {
        this.overallFulfillmentStatus = overallFulfillmentStatus;
    }

    // Persisted refund totals for downstream services
    @Column(name = "total_refund_amount", nullable = true, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalRefundAmount = BigDecimal.ZERO; // negative value

    // GST component of refund across all items (negative)
    @Column(name = "total_refund_gst_amount", nullable = true, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalRefundGstAmount = BigDecimal.ZERO; // negative value

    // Combined refund including GST (negative)
    @Column(name = "total_refund_including_gst", nullable = true, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalRefundIncludingGst = BigDecimal.ZERO; // totalRefundAmount + totalRefundGstAmount

    @Column(name = "net_total_amount", nullable = true, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal netTotalAmount = BigDecimal.ZERO; // totalAmount + totalRefundAmount

    // Fee breakdown — saved at order creation so friendlySummary always shows exact values
    @Column(name = "platform_fee", nullable = true, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "packaging_fee", nullable = true, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal packagingFee = BigDecimal.ZERO;

    @Column(name = "surge_fee", nullable = true, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal surgeFee = BigDecimal.ZERO;

    // Zoho Books invoice ID (set after invoice is created)
    @Column(name = "zoho_invoice_id", length = 100)
    private String zohoInvoiceId;

    // One-to-many relationship with OrderItems
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    // One-to-one relationship with OrderDelivery
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrderDelivery delivery;

    // One-to-one relationship with OrderPayment
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrderPayment payment;

    public enum OrderStatus {
        // ── Restaurant vertical statuses (v1 spec) ───────────────────────────
        PLACED,               // Customer placed the order; awaiting restaurant confirmation
        RESTAURANT_CONFIRMED, // Restaurant accepted the order
        PREPARING,            // Kitchen is actively preparing the order
        READY,                // Order ready for driver pickup
        DRIVER_ASSIGNED,      // A delivery driver has been assigned
        PICKED_UP,            // Driver collected order from restaurant/store
        OUT_FOR_DELIVERY,     // Driver is on the way to customer
        DELIVERED,            // Order delivered to customer
        REJECTED,             // Restaurant/store rejected the order
        CANCELLED,            // Cancelled by customer or system
        FAILED,               // Payment or technical failure
        // ── Grocery vertical statuses (unchanged, no migration) ─────────────────
        PENDING,              // Grocery: order placed, awaiting store confirmation
        CONFIRMED,            // Grocery: store confirmed
        PACKED,               // Grocery: order packed for pickup
        SHIPPED,              // Grocery: picked up by delivery partner
        REFUNDED              // Shared: order has been refunded
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    public boolean canBeCancelled() {
        if ("RESTAURANT".equals(storeVertical)) {
            // Restaurant: only before kitchen starts
            return status == OrderStatus.PLACED || status == OrderStatus.RESTAURANT_CONFIRMED;
        }
        // Grocery: only before store processes
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    // ── Grocery transition helpers (original behaviour, untouched) ──────────

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order can only be confirmed from PENDING status");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void pack() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order can only be packed from CONFIRMED status");
        }
        this.status = OrderStatus.PACKED;
        this.packedAt = LocalDateTime.now();
    }

    public void ship() {
        if (status != OrderStatus.PACKED) {
            throw new IllegalStateException("Order can only be shipped from PACKED status");
        }
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    public void markOutForDelivery() {
        if (status != OrderStatus.SHIPPED && status != OrderStatus.PICKED_UP) {
            throw new IllegalStateException("Order can only be marked out for delivery from SHIPPED/PICKED_UP status");
        }
        this.status = OrderStatus.OUT_FOR_DELIVERY;
    }

    public void deliver() {
        if (status != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("Order can only be delivered from OUT_FOR_DELIVERY status");
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    // ── Restaurant-specific transition helpers ───────────────────────────────

    /** Restaurant accepts the order. */
    public void restaurantConfirm() {
        if (status != OrderStatus.PLACED && status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order must be in PLACED status to confirm");
        }
        this.status = OrderStatus.RESTAURANT_CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    /** Restaurant starts preparing. */
    public void startPreparing() {
        if (status != OrderStatus.RESTAURANT_CONFIRMED && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be RESTAURANT_CONFIRMED before PREPARING");
        }
        this.status = OrderStatus.PREPARING;
        this.preparingAt = LocalDateTime.now();
    }

    /** Restaurant marks order ready for driver pickup. */
    public void markReady() {
        if (status != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order must be PREPARING before READY");
        }
        this.status = OrderStatus.READY;
        this.readyAt = LocalDateTime.now();
    }

    /** Delivery system assigns a driver. */
    public void assignDriver() {
        if (status != OrderStatus.READY && status != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order must be READY or PREPARING to assign driver");
        }
        this.status = OrderStatus.DRIVER_ASSIGNED;
        this.driverAssignedAt = LocalDateTime.now();
    }

    /** Driver picks up the order. */
    public void driverPickUp() {
        if (status != OrderStatus.DRIVER_ASSIGNED && status != OrderStatus.READY) {
            throw new IllegalStateException("Order must have a driver assigned before pickup");
        }
        this.status = OrderStatus.PICKED_UP;
        this.pickedUpAt = LocalDateTime.now();
        this.shippedAt = LocalDateTime.now(); // legacy compat
    }

    /** Restaurant rejects the order. */
    public void reject(String reason) {
        if (status != OrderStatus.PLACED && status != OrderStatus.PENDING
                && status != OrderStatus.RESTAURANT_CONFIRMED) {
            throw new IllegalStateException("Order can only be rejected before preparation starts");
        }
        this.status = OrderStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /** Mark order as failed (payment or technical failure). */
    public void fail(String reason) {
        this.status = OrderStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public boolean isTerminal() {
        return status == OrderStatus.DELIVERED
            || status == OrderStatus.CANCELLED
            || status == OrderStatus.REJECTED
            || status == OrderStatus.FAILED
            || status == OrderStatus.REFUNDED;
    }
}
