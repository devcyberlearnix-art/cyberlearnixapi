package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingCourseResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String thumbnail;
    private String category;
    private String level;
    private String language;
    private Boolean premium;
    private BigDecimal price;
    private Double rating;
    private Long enrollmentCount;
    private Long searchCount;
    private Long viewCount;
    private Double trendingScore;
}