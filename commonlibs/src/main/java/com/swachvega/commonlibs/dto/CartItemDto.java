package com.cyberlearnix.commonlibs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private String productName;
    private String productBrand;
    private String productImage;
    private String gtin;
    private Long storeId;
    private String storeName;
    private Double mrp;
    private Double price;
    private Integer quantity;
    private Double discountPercentage;
    private Double finalPrice;
    private Double weightPerUnit;
    private Double totalWeight;
    private String weightUnit;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
    private Integer availableStock;
    private boolean inStock;
    
    // GST fields
    private Double gstRate;
    private Double igstRate;
    private Double cgstRate;
    private Double sgstRate;
    private Double gstAmount;
    private Double igstAmount;
    private Double cgstAmount;
    private Double sgstAmount;
    private Double totalGstAmount;
    private Double priceBeforeGst;
    private Double finalPriceWithGst;
    private Double savingsAmount; // (mrp - price) * quantity, null when no savings

    // Ensure two-decimal precision in JSON for item weights
    public Double getWeightPerUnit() {
        if (weightPerUnit == null) return null;
        return java.math.BigDecimal.valueOf(weightPerUnit)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    public Double getTotalWeight() {
        if (totalWeight == null) return null;
        return java.math.BigDecimal.valueOf(totalWeight)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    public Double getGstAmount() {
        if (gstAmount == null) return null;
        return java.math.BigDecimal.valueOf(gstAmount)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue() == 0.0 ? null : gstAmount;
    }

    public Double getIgstAmount() {
        if (igstAmount == null) return null;
        return java.math.BigDecimal.valueOf(igstAmount)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue() == 0.0 ? null : igstAmount;
    }
}
