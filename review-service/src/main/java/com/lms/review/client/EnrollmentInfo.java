package com.lms.review.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentInfo {
    private Long id;
    private Long courseId;
    private UUID userId;
    private String status;
}
