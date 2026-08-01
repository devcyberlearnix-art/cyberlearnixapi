package com.lms.coupon_service.dto;
import lombok.Data;

@Data
public class RedeemRequest {
    private String couponCode;
    private Long courseId;
}