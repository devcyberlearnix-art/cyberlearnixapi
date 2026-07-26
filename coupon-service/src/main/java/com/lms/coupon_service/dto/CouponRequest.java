package com.lms.coupon_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponRequest {
    private String couponCode;
    private String userId;
    private String courseId;
    private Double price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String creatorRole;
}