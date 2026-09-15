package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseMaterialsResponse {
    private boolean success;
    private String message;
    private CourseMaterialsData data;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseMaterialsData {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private String materialName;
        private String materialType;
        private String fileUrl;
        private Long fileSize;
    }
}