package com.lms.coupon_service.dto;

import lombok.Data;

@Data
public class ValidateRequest {
    private String couponCode;
    private Long courseId;
    private Double price;
}