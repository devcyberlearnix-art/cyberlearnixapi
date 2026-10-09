package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailsDTO {
    private CourseInfoDTO courseInfo;
    private InstructorProfileDTO instructorProfile;
    private CourseCurriculumDTO curriculum;
    private EnrollmentStatusDTO enrollmentStatus;
    private RatingSummaryDTO ratingSummary;
    private CourseStatsDTO courseStats;
    private List<CourseRequirementsDTO> requirements;
    private List<LearningOutcomesDTO> learningOutcomes;
    private List<CourseMaterialsDTO> materials;
    private List<CourseFAQDTO> faqs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseInfoDTO {
        private Long id;
        private String title;
        private String subtitle;
        private String description;
        private String category;
        private String level;
        private String language;
        private BigDecimal price;
        private String thumbnail;
        private UUID instructorId;
        private String status;
        private Boolean premium;
        private Long searchCount;
        private Long viewCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorProfileDTO {
        private UUID instructorId;
        private String name;
        private String email;
        private String headline;
        private String bio;
        private List<String> expertise;
        private Double rating;
        private Integer totalCourses;
        private Integer totalStudents;
        private Boolean verified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseCurriculumDTO {
        private Long courseId;
        private String courseTitle;
        private Integer totalDuration;
        private Integer totalSections;
        private Integer totalLectures;
        private List<SectionDTO> sections;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionDTO {
        private Long sectionId;
        private String title;
        private Integer orderIndex;
        private Integer sectionDuration;
        private Integer lectureCount;
        private List<LectureDTO> lectures;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LectureDTO {
        private Long lectureId;
        private String title;
        private String description;
        private String videoUrl;
        private Integer duration;
        private Integer orderIndex;
        private Boolean previewEnabled;
        private String resources;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollmentStatusDTO {
        private Boolean isEnrolled;
        private String enrollmentDate;
        private Double progress;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingSummaryDTO {
        private Double averageRating;
        private Long totalRatings;
        private Long totalReviews;
        private Long fiveStarCount;
        private Long fourStarCount;
        private Long threeStarCount;
        private Long twoStarCount;
        private Long oneStarCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStatsDTO {
        private Long totalEnrollments;
        private Long totalStudents;
        private Double averageProgress;
        private Long completionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseRequirementsDTO {
        private Long id;
        private String requirementType;
        private String requirementText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningOutcomesDTO {
        private Long id;
        private String outcomeText;
        private String skillCategory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseMaterialsDTO {
        private Long id;
        private String materialName;
        private String materialType;
        private String fileUrl;
        private Long fileSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseFAQDTO {
        private Long id;
        private String question;
        private String answer;
        private Integer displayOrder;
    }
}
