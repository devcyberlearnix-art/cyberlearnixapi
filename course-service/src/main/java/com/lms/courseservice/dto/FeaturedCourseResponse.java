package com.lms.courseservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class FeaturedCourseResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String level;
    private String language;
    private BigDecimal price;
    private String thumbnail;
    private UUID instructorId;
    private String status;
    private boolean premium;
    private long students;
    private double rating;
    private long totalReviews;
    private long searchCount;
    private long viewCount;
    private double featuredScore;
    private String tag;
}