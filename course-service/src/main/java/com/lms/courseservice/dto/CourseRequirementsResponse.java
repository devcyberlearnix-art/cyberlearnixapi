package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequirementsResponse {
    private boolean success;
    private String message;
    private CourseRequirementsData data;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseRequirementsData {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private String requirementType;
        private String requirementText;
    }
}