package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseContentResponse {
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseThumbnail;
    private Integer totalSections;
    private Integer totalLectures;
    private Integer totalDuration;
    private Double overallProgress;
    private List<SectionContentDTO> sections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionContentDTO {
        private Long sectionId;
        private String title;
        private Integer orderIndex;
        private Integer sectionDuration;
        private Integer lectureCount;
        private List<LectureContentDTO> lectures;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LectureContentDTO {
        private Long lectureId;
        private String title;
        private String description;
        private String videoUrl;
        private Integer duration;
        private Integer orderIndex;
        private Boolean previewEnabled;
        private String resources;
        private LectureProgressInfo progress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LectureProgressInfo {
        private String status;
        private Integer progressPercentage;
        private Boolean isCompleted;
    }
}
