package com.cyberlearnix.commonlibs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long cartId;
    private UUID userId;
    private String userName; // User display name
    private Long storeId;    // Single-store cart: store id
    private UUID merchantId; // Merchant registration id for the store
    private Double totalWeight;
    private Double totalAmount;
    private Integer totalItems;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private List<CartItemDto> cartItems;
    private boolean expired;
    private Double maxWeightAllowed;
    private Double remainingWeight;
    
    // GST fields
    private Double totalGstAmount;
    private Double totalAmountWithGst;

    private String appliedCouponCode;
    private Double appliedCouponDiscount;

    // Delivery estimate (calculated dynamically)
    private Double estimatedDeliveryFee;
    private DeliveryEstimateBreakdown deliveryEstimateBreakdown;

    // Ensure two-decimal precision in JSON for weights
    public Double getTotalWeight() {
        if (totalWeight == null) return null;
        return java.math.BigDecimal.valueOf(totalWeight)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    public Double getRemainingWeight() {
        if (remainingWeight == null) return null;
        return java.math.BigDecimal.valueOf(remainingWeight)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Data
    public static class DeliveryEstimateBreakdown {
        private String deliveryType;
        private Double distanceKm;
        private Double baseRate;
        private Double perKmComponent;
        private Double weightSurchargeComponent;
        private Double typeMultiplier;
        private Double prePromotionCharge;
        private String promotionId;
        private String promotionName;
        private Double discountAmount;
        private Double finalCharge;
        // Delivery GST details
        private Double deliveryGstRate;       // percent
        private Double deliveryGstAmount;     // GST on delivery fee estimate
        private Double finalChargeWithGst;    // finalCharge + deliveryGstAmount
        // Optional fees and coupon support (additive, safe for old clients)
        private Double platformFee;
        private Double packagingFee;
        private Double surgeFee;
        private String couponCode;
        private Double couponDiscount;
    }

    @Data
    public static class GstOtherCharges {
        private Double gstOnItems;         // preferred: GST on items
        private Double gstOnDeliveryFee;   // from deliveryEstimateBreakdown.deliveryGstAmount
        private Double platformFee;        // inclusive of GST if applicable
        private Double packagingFee;
        private Double surgeFee;
        private Double total;              // sum of above
    }

    @Data
    public static class FriendlySummary {
        private Double itemTotal;          // totalAmount
        private Double deliveryFee;        // deliveryEstimateBreakdown.finalChargeWithGst (or finalCharge)
        private String distanceLabel;      // e.g., "3.9 kms"
        private Double tip;                // optional, currently null/0
        private GstOtherCharges gstAndOtherCharges;
        private Double couponDiscount;     // applied discount, if any
        private Double toPay;              // grand total amount to pay
        private String savingsMessage;     // e.g., "₹11 saved on the total!"
        private Double mrpTotal;           // sum of mrp * qty
        private Double storePriceTotal;    // sum of price * qty
        private Boolean distanceExceedsLimit; // true if delivery distance > max allowed
        private String distanceWarning;    // user-facing message when distance exceeds limit
    }

    // Optional: id of the selected/default delivery address and the full address string
    // for display in main /api/cart response.
    private String deliveryAddressId;
    private String deliveryAddress;

    private FriendlySummary friendlySummary;

    private String message;
}
