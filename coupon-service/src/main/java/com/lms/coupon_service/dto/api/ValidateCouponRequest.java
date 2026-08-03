package com.lms.coupon_service.dto.api;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Public DTO for coupon validation from frontend.
 * Does NOT contain userId - it will be extracted from JWT.
 */
@Data
public class ValidateCouponRequest {
    
    @NotBlank(message = "Coupon code is required")
    private String couponCode;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
}
