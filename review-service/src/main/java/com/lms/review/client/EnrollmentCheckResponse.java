package com.lms.review.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentCheckResponse {
    private boolean success;
    private String message;
    private EnrollmentCheckData data;

    public boolean isEnrolled() {
        return data != null && data.isEnrolled();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollmentCheckData {
        private Long courseId;
        private String courseName;
        private java.util.UUID studentId;
        private boolean enrolled;
        private String enrollmentStatus;
    }
}
