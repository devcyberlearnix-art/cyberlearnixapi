package com.cyberlearnix.commonlibs.dto;

import com.cyberlearnix.commonlibs.entity.Order;
import com.cyberlearnix.commonlibs.entity.OrderDelivery;
import com.cyberlearnix.commonlibs.entity.OrderPayment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class OrderResponse {

    private Long orderId;
    private UUID userId;
    private Long storeId;
    private java.util.UUID merchantId;
    private Long cartId;
    private String orderNumber;
    private Order.OrderStatus status;
    private String overallFulfillmentStatus;
    private Double totalAmount;
    private Double subtotal;
    private Double deliveryFee;
    private Double tax;
    private Double discount;
    private Integer totalItems;
    private Double totalWeight;
    private LocalDateTime orderDate;
    private LocalDateTime confirmedAt;
    private LocalDateTime packedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String specialInstructions;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Refund totals
    private Double totalRefundAmount; // negative
    private Double netTotalAmount;    // totalAmount + totalRefundAmount

    // Fee breakdown saved at order creation
    private Double platformFee;
    private Double packagingFee;
    private Double surgeFee;

    // Order items
    private List<OrderItemResponse> orderItems;

    // Delivery information
    private OrderDeliveryResponse delivery;
    private DeliveryBreakdown deliveryBreakdown; // step-by-step delivery charge calculation

    // Payment information
    private OrderPaymentResponse payment;

    // Store information (from external service)
    private String storeName;
    private String storeAddress;
    private String storePhone;

    // Optional friendly summary similar to cart API, for easy client consumption
    private FriendlySummary friendlySummary;

    public String getOverallFulfillmentStatus() {
        return overallFulfillmentStatus;
    }

    public void setOverallFulfillmentStatus(String overallFulfillmentStatus) {
        this.overallFulfillmentStatus = overallFulfillmentStatus;
    }

    @Data
    public static class GstOtherCharges {
        private Double gstOnItems;
        private Double gstOnDeliveryFee;
        private Double platformFee;
        private Double packagingFee;
        private Double surgeFee;
        private Double total;
    }

    @Data
    public static class FriendlySummary {
        private Double itemTotal;
        private Double deliveryFee;
        private Double tip;
        private GstOtherCharges gstAndOtherCharges;
        private Double couponDiscount;
        private Double toPay;
        private String savingsMessage;
        private Double mrpTotal;
        private Double storePriceTotal;
    }

    // Ensure two-decimal precision in JSON for totalWeight
    public Double getTotalWeight() {
        if (totalWeight == null) return null;
        return BigDecimal.valueOf(totalWeight)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Data
    public static class DeliveryBreakdown {
        private String deliveryType; // standard/express/scheduled
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
        private Double deliveryGstAmount;     // GST on delivery fee
        private Double finalChargeWithGst;    // finalCharge + deliveryGstAmount
    }

    @Data
    public static class OrderItemResponse {
        private Long orderItemId;
        private Long productId;
        private String productName;
        private String productDescription;
        private String productImageUrl;
        private Integer quantity;
        private Integer originalQuantity; // initial ordered qty
        private Double unitPrice;
        private Double totalPrice;
        private Double unitWeight;
        private Double totalWeight;
        private String unit;
        private String brand;
        private String category;
        private String specialInstructions;
        // Fulfillment status set by merchant review/packing (packed/partial/not_available)
        private String fulfillmentStatus;
        // GST breakdown (optional, if available)
        private Double gstRate;
        private Double igstRate;
        private Double cgstRate;
        private Double sgstRate;
        private Double gstAmount;
        private Double igstAmount;
        private Double cgstAmount;
        private Double sgstAmount;
        private Double totalGstAmount;
        private Double originalTotalGstAmount; // initial total GST at ordered qty
        private Double priceBeforeGst;
        private Double totalPriceWithGst;
        // Refund info
        private Integer refundQuantity;
        private Double refundAmount; // negative
        private String refundReason;
    }

    @Data
    public static class OrderDeliveryResponse {
        private Long deliveryId;
        private OrderDelivery.DeliveryStatus status;
        private OrderDelivery.DeliveryType type;
        private String deliveryAddress;
        private String city;
        private String state;
        private String zipCode;
        private String customerPhone;
        private String customerName;
        private String deliveryPartnerName;
        private String deliveryPartnerPhone;
        private String vehicleNumber;
        private String trackingNumber;
        private LocalDateTime estimatedPickupTime;
        private LocalDateTime actualPickupTime;
        private LocalDateTime estimatedDeliveryDate;
        private LocalDateTime actualDeliveryDate;
        private Double currentLatitude;
        private Double currentLongitude;
        private Double distanceKm;
        private LocalDateTime lastLocationUpdate;
        private String deliveryInstructions;
        private String deliveryNotes;
        private String failureReason;
    }

    @Data
    public static class OrderPaymentResponse {
        private Long paymentId;
        private OrderPayment.PaymentMethod paymentMethod;
        private OrderPayment.PaymentStatus status;
        private Double amount;
        private Double paidAmount;
        private Double refundedAmount;
        private String transactionId;
        private String paymentGateway;
        private String paymentReference;
        private LocalDateTime paymentDate;
        private LocalDateTime refundDate;
        private String failureReason;
        private String refundReason;
    }
}
