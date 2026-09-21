package com.lms.courseservice.dto;

import com.lms.courseservice.entity.Banner.BannerTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicBannerResponse {

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
    private BannerTargetType targetType;
}
