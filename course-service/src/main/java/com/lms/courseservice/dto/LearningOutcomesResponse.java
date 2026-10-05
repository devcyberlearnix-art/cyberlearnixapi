package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningOutcomesResponse {
    private boolean success;
    private String message;
    private LearningOutcomesData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningOutcomesData {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private String outcomeText;
        private String skillCategory;
    }
}
