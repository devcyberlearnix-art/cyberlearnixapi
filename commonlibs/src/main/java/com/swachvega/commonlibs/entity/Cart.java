package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_user_id", columnList = "userId"),
        @Index(name = "idx_cart_created_at", columnList = "createdAt")
})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "total_weight")
    private Double totalWeight = 0.0;

    @Column(name = "total_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE, ABANDONED, CONVERTED_TO_ORDER

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // GST fields for cart totals
    @Column(name = "total_gst_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalGstAmount = BigDecimal.ZERO;

    @Column(name = "total_amount_with_gst", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalAmountWithGst = BigDecimal.ZERO;

    @Column(name = "applied_coupon_code")
    private String appliedCouponCode;

    @Column(name = "applied_coupon_discount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal appliedCouponDiscount = BigDecimal.ZERO;

    // Last delivery address selected by user on this cart (saved so OrderService can auto-pick it)
    @Column(name = "delivery_address_id")
    private UUID deliveryAddressId;

    // Cached delivery estimate values (persisted so DB is never NULL for these columns)
    @Column(name = "estimated_delivery_fee_pre_gst")
    private Double estimatedDeliveryFeePreGst;

    @Column(name = "estimated_delivery_gst_amount")
    private Double estimatedDeliveryGstAmount;

    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;

    // When true, the delivery estimate fields above are locked (e.g. payment initiated)
    // and should NOT be overwritten by subsequent cart API calls.
    @Column(name = "delivery_estimate_locked")
    private Boolean deliveryEstimateLocked = false;

    // Fee snapshot — saved alongside delivery estimate so OrderService always uses
    // the exact same fee values the customer saw in the cart (not a fresh Redis read).
    @Column(name = "platform_fee")
    private Double platformFee;

    @Column(name = "packaging_fee")
    private Double packagingFee;

    @Column(name = "surge_fee")
    private Double surgeFee;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        // Set expiration to 7 days from creation if not set
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plusDays(7);
        }
    }

    // Helper methods
    public void calculateTotals() {
        this.totalItems = cartItems.size();

        if (cartItems.isEmpty()) {
            this.appliedCouponCode = null;
            this.appliedCouponDiscount = BigDecimal.ZERO;
        }
        
        // Calculate total amount without GST (for backward compatibility)
        this.totalAmount = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate total GST amount
        this.totalGstAmount = cartItems.stream()
                .map(CartItem::getTotalGstAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        
        // totalAmount IS already GST-inclusive (Indian pricing), so totalAmountWithGst equals totalAmount.
        // totalGstAmount is the extracted GST component shown for transparency.
        this.totalAmountWithGst = this.totalAmount;
        
        // Calculate total weight
        this.totalWeight = cartItems.stream()
                .mapToDouble(item -> item.getTotalWeight() != null ? item.getTotalWeight() : 0.0)
                .sum();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canAddItemFromStore(Long storeId) {
        // Check if cart is empty or all items are from the same store
        if (cartItems.isEmpty()) {
            return true;
        }
        
        // Get the store ID from the first item (all items must be from same store)
        Long cartStoreId = cartItems.get(0).getStoreId();
        return cartStoreId.equals(storeId);
    }
    
    /**
     * Get the store ID of items in this cart (if any)
     * @return storeId if cart has items, null if empty
     */
    public Long getCartStoreId() {
        return cartItems.isEmpty() ? null : cartItems.get(0).getStoreId();
    }
}
