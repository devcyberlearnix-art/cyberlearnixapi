package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseFAQResponse {
    private boolean success;
    private String message;
    private CourseFAQData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseFAQData {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private String question;
        private String answer;
        private Integer displayOrder;
    }
}
