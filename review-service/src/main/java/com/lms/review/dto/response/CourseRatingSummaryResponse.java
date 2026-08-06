package com.lms.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingSummaryResponse {

    private Long courseId;
    private Double averageRating;
    private Long totalReviews;
    private Map<String, Long> ratingDistribution;
}
