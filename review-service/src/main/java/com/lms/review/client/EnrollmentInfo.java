package com.lms.review.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentInfo {
    private Long courseId;
    private String courseTitle;
    private UUID studentId;
    private String category;
    private String status;
    private String enrolledAt;
}
