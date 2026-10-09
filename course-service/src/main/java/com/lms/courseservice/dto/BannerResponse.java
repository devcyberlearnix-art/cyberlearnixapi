package com.lms.courseservice.dto;

import com.lms.courseservice.entity.Banner.BannerStatus;
import com.lms.courseservice.entity.Banner.BannerTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse {

    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String mobileImageUrl;
    private String altText;
    private String ctaText;
    private String ctaUrl;
    private Long courseId;
    private Integer displayOrder;
    private BannerStatus status;
    private BannerTargetType targetType;
    private Instant startDate;
    private Instant endDate;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long impressions;
    private Long clicks;
    private Long conversions;
    private Instant lastImpressionAt;
    private Instant lastClickAt;
}
