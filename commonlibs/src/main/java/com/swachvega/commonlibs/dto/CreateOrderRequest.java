package com.cyberlearnix.commonlibs.dto;

import com.cyberlearnix.commonlibs.entity.OrderPayment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    @NotNull(message = "Payment method is required")
    private OrderPayment.PaymentMethod paymentMethod;

    // Delivery information
    // Optional: when not provided, orderservice will derive the delivery address
    // from the user's selected/default address used for the cart.
    @Size(max = 500, message = "Delivery address cannot exceed 500 characters")
    private String deliveryAddress;

    // Optional: ID of the selected delivery address (UUID string) used when building the cart.
    // When present, orderservice will forward this to cartservice so that the exact same
    // user address is resolved for the order's deliveryAddress.
    private String selectedAddressId;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    private String zipCode;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String customerPhone;

    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;

    @Size(max = 500, message = "Special instructions cannot exceed 500 characters")
    private String specialInstructions;

    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;

    // Payment information (for non-COD payments)
    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    private String transactionId;

    /** PayU's mihpayid — returned by PayU on payment success */
    @Size(max = 100, message = "PayU transaction ID cannot exceed 100 characters")
    private String payuTransactionId;

    @Size(max = 100, message = "Payment gateway cannot exceed 100 characters")
    private String paymentGateway;

    @Size(max = 500, message = "Payment details cannot exceed 500 characters")
    private String paymentDetails;

    // Optional amounts to keep order totals in sync with cart summary
    private Double tipAmount;          // Included in total (non-taxable)
    private Double platformFee;        // Included in total; assumed tax-included if applicable
    private Double packagingFee;       // Included in total
    private Double surgeFee;           // Included in total
    private String couponCode;         // For reconciliation
    private Double couponDiscount;     // Subtracted from total
    private List<String> couponCodes;  // New: multiple coupon codes for stacking
    private String deliveryType;       // e.g., standard/express
    private Double distanceKm;         // Optional, for breakdown mapping
    private Double deliveryFeePreGst;  // Optional: deliveryEstimateBreakdown.finalCharge from cart
    private Double deliveryGstAmount;  // Optional: deliveryEstimateBreakdown.deliveryGstAmount from cart
}
