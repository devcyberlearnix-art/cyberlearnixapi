package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingSummary {
    private Long courseId;
    private Double averageRating;
    private Long totalReviews;
}