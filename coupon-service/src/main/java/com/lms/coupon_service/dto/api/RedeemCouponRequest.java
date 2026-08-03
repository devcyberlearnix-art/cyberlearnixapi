package com.lms.coupon_service.dto.api;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Public DTO for coupon redemption from frontend.
 * Does NOT contain userId - it will be extracted from JWT.
 */
@Data
public class RedeemCouponRequest {
    
    @NotBlank(message = "Coupon code is required")
    private String couponCode;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
}
