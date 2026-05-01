package com.lms.cart_service.dto;

import lombok.Data;

@Data
public class CouponResponse {
    private String couponCode;
    private Double discountPercentage;
    private boolean valid;
}