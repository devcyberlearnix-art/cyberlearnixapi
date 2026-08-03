package com.lms.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmissionResponse {
    private String reviewId;
    private String courseId;
    private String studentId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
