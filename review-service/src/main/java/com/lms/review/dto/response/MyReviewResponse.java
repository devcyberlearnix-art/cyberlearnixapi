package com.lms.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyReviewResponse {

    private String reviewId;
    private String courseId;
    private String studentId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
}
