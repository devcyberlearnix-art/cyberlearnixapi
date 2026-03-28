package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart_id", columnList = "cartId"),
        @Index(name = "idx_cart_item_store_product", columnList = "storeId,productId")
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;

    // Store and Product IDs to identify the inventory item
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Reference to the inventory item (product at this store)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "store_id", referencedColumnName = "store_id", insertable = false, updatable = false),
        @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false)
    })
    private Inventory inventory;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal price;

    @Column(name = "discount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal discount = BigDecimal.ZERO; // Interpreted as percentage (0-100)

    @Column(name = "final_price", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal finalPrice;

    @Column(name = "weight_per_unit")
    private Double weightPerUnit;

    @Column(name = "total_weight")
    private Double totalWeight;

    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // GST fields
    @Column(name = "gst_rate", precision = 5, scale = 2)
    private BigDecimal gstRate = BigDecimal.ZERO;

    @Column(name = "igst_rate", precision = 5, scale = 2)
    private BigDecimal igstRate = BigDecimal.ZERO;

    @Column(name = "cgst_rate", precision = 5, scale = 2)
    private BigDecimal cgstRate = BigDecimal.ZERO;

    @Column(name = "sgst_rate", precision = 5, scale = 2)
    private BigDecimal sgstRate = BigDecimal.ZERO;

    @Column(name = "gst_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "igst_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "cgst_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "total_gst_amount", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal totalGstAmount = BigDecimal.ZERO;

    @Column(name = "price_before_gst", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal priceBeforeGst = BigDecimal.ZERO;

    @Column(name = "final_price_with_gst", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal finalPriceWithGst = BigDecimal.ZERO;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateFinalPrice();
        calculateTotalWeight();
    }

    @PrePersist
    public void prePersist() {
        if (this.addedAt == null) {
            this.addedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        calculateFinalPrice();
        calculateTotalWeight();
    }

    // Helper methods
    public void calculateFinalPrice() {
        BigDecimal p = this.price != null ? this.price : BigDecimal.ZERO;
        BigDecimal qty = BigDecimal.valueOf(this.quantity != null ? this.quantity : 0);
        BigDecimal lineTotal = p.multiply(qty).setScale(2, RoundingMode.HALF_UP);

        // Indian MRP/store prices are GST-inclusive.
        // Extract the pre-GST base: priceBeforeGst = lineTotal / (1 + effectiveRate/100)
        BigDecimal effectiveRate = effectiveGstRate();
        if (effectiveRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal divisor = BigDecimal.ONE.add(effectiveRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            this.priceBeforeGst = lineTotal.divide(divisor, 2, RoundingMode.HALF_UP);
        } else {
            this.priceBeforeGst = lineTotal;
        }

        // Calculate extracted GST amounts
        calculateGstAmounts();

        // Customer pays lineTotal (GST-inclusive)
        this.finalPrice = lineTotal;
        this.finalPriceWithGst = lineTotal;
    }

    /** Combined effective GST rate from CGST+SGST or generic GST rate. */
    private BigDecimal effectiveGstRate() {
        boolean hasCgst = this.cgstRate != null && this.cgstRate.compareTo(BigDecimal.ZERO) > 0;
        boolean hasSgst = this.sgstRate != null && this.sgstRate.compareTo(BigDecimal.ZERO) > 0;
        if (hasCgst || hasSgst) {
            BigDecimal combined = BigDecimal.ZERO;
            if (hasCgst) combined = combined.add(this.cgstRate);
            if (hasSgst) combined = combined.add(this.sgstRate);
            return combined;
        }
        return this.gstRate != null ? this.gstRate : BigDecimal.ZERO;
    }
    
    public void calculateGstAmounts() {
        if (this.priceBeforeGst == null) {
            this.priceBeforeGst = BigDecimal.ZERO;
        }

        BigDecimal p = this.price != null ? this.price : BigDecimal.ZERO;
        BigDecimal qty = BigDecimal.valueOf(this.quantity != null ? this.quantity : 0);
        BigDecimal lineTotal = p.multiply(qty).setScale(2, RoundingMode.HALF_UP);
        // Extracted GST = inclusive total - base (guaranteed exact after rounding)
        BigDecimal extractedGst = lineTotal.subtract(this.priceBeforeGst);

        // Initialize to zero
        this.gstAmount = BigDecimal.ZERO;
        this.igstAmount = BigDecimal.ZERO; // IGST ignored by policy
        this.cgstAmount = BigDecimal.ZERO;
        this.sgstAmount = BigDecimal.ZERO;

        // Split extracted GST among CGST/SGST or generic GST
        boolean hasCgst = this.cgstRate != null && this.cgstRate.compareTo(BigDecimal.ZERO) > 0;
        boolean hasSgst = this.sgstRate != null && this.sgstRate.compareTo(BigDecimal.ZERO) > 0;
        boolean hasGst = this.gstRate != null && this.gstRate.compareTo(BigDecimal.ZERO) > 0;

        if (hasCgst || hasSgst) {
            if (hasCgst && hasSgst) {
                BigDecimal combined = this.cgstRate.add(this.sgstRate);
                this.cgstAmount = extractedGst.multiply(this.cgstRate)
                        .divide(combined, 2, RoundingMode.HALF_UP);
                this.sgstAmount = extractedGst.subtract(this.cgstAmount); // remainder avoids rounding gap
            } else if (hasCgst) {
                this.cgstAmount = extractedGst;
            } else {
                this.sgstAmount = extractedGst;
            }
        } else if (hasGst) {
            this.gstAmount = extractedGst;
        }

        // Total GST amount (sum of selected GST types; IGST excluded)
        this.totalGstAmount = this.gstAmount.add(this.cgstAmount).add(this.sgstAmount);
    }
    
    private BigDecimal calculateGstAmount(BigDecimal baseAmount, BigDecimal rate) {
        if (baseAmount == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return baseAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public void calculateTotalWeight() {
        this.totalWeight = (this.weightPerUnit != null ? this.weightPerUnit : 0.0) * this.quantity;
    }

    public BigDecimal getTotalPrice() {
        if (this.finalPrice != null) return this.finalPrice;
        BigDecimal p = this.price != null ? this.price : BigDecimal.ZERO;
        BigDecimal qty = BigDecimal.valueOf(this.quantity != null ? this.quantity : 0);
        return p.multiply(qty).setScale(2, RoundingMode.HALF_UP);
    }
    
    // Helper methods to access product and store through inventory
    public Product getProduct() {
        return inventory != null ? inventory.getProduct() : null;
    }
    
    public StoreEntity getStore() {
        return inventory != null ? inventory.getStore() : null;
    }
}
