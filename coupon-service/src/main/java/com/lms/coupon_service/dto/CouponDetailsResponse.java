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
public class CouponDetailsResponse {
    private String couponId;
    private String code;
    private String title;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double minimumOrderAmount;
    private Double maximumDiscountAmount;
    private Integer usageLimit;
    private Integer usageLimitPerUser;
    private String status;
    private Instant validFrom;
    private Instant validUntil;
    private CreatedBy createdBy;
    private Instant createdAt;
    private List<String> applicableCourseIds;
    private Boolean stackable;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreatedBy {
        private String id;
        private String role;
    }
}
