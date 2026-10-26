package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureDetailResponse {
    private Long lectureId;
    private String title;
    private String description;
    private String videoUrl;
    private Integer duration;
    private Integer orderIndex;
    private Boolean previewEnabled;
    private String resources;
    private SectionInfo section;
    private CourseInfo course;
    private LectureProgressInfo progress;
    private NavigationInfo navigation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionInfo {
        private Long sectionId;
        private String title;
        private Integer orderIndex;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseInfo {
        private Long courseId;
        private String title;
        private String thumbnail;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NavigationInfo {
        private Long previousLectureId;
        private String previousLectureTitle;
        private Long nextLectureId;
        private String nextLectureTitle;
    }
}
