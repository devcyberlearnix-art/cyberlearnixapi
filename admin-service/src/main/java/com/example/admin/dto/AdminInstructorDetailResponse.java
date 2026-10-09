package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminInstructorDetailResponse {

    private boolean success;
    private String message;
    private String timestamp;
    private InstructorDetailData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InstructorDetailData {
        private InstructorProfile instructor;
        private InstructorMetrics metrics;
        private List<InstructorCourseData> courses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InstructorProfile {
        private UUID userId;
        private Long instructorId;
        private String firstName;
        private String lastName;
        private String email;
        private String mobile;
        private String profilePhoto;
        private String bio;
        private String specialization;
        private String skills;
        private String highestQualification;
        private String organization;
        private String fieldOfStudy;
        private String city;
        private String state;
        private String country;
        private String preferredLanguage;
        private String status;
        private String appliedRole;
        private Boolean isInstructorApproved;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorMetrics {
        private int totalCourses;
        private int publishedCourses;
        private int draftCourses;
        private int archivedCourses;
        private int totalStudents;
        private double totalRevenue;
        private double averageRating;
        private int totalReviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InstructorCourseData {
        private Long courseId;
        private String title;
        private String subtitle;
        private String description;
        private String slug;
        private String category;
        private String level;
        private String language;
        private BigDecimal price;
        private String thumbnail;
        private String status;
        private int enrolledStudents;
        private double revenue;
        private double averageRating;
        private int totalReviews;
        private double completionRate;
        private String createdAt;
        private String publishedAt;
        private CourseContentSummary contentSummary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CourseContentSummary {
        private int sections;
        private int lectures;
        private int assignments;
        private int quizzes;
        private int totalDurationMinutes;
    }
}
