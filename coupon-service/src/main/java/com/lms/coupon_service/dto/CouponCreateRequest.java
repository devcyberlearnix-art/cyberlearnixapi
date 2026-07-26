package com.lms.coupon_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponCreateRequest {
    private String code;
    private String title;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double minimumOrderAmount;
    private Double maximumDiscountAmount;
    private Integer usageLimit;
    private Integer usageLimitPerUser;
    private Instant validFrom;
    private Instant validUntil;
    private String applicableTo;
    private List<String> applicableCourseIds;
    private Boolean stackable;
    private String status;
}
