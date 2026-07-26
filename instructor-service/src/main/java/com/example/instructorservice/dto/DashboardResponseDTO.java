
package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DashboardResponseDTO {
    private boolean success;
    private String message;
    private String requestId;
    private LocalDateTime timestamp;
    private DashboardData data;

    @Data
    @Builder
    public static class DashboardData {
        private int totalCourses;
        private int publishedCourses;
        private int draftCourses;
        private int archivedCourses;
        private int totalStudents;
        private double totalRevenue;
        private double averageRating;
        private List<CourseData> courses; // per-course analytics
    }

    @Data
    @Builder
    public static class CourseData {
        private Long courseId;
        private String title;
        private String slug;
        private String status;
        private int enrolledStudents;
        private double revenue;
        private double averageRating;
        private double completionRate;
        private LocalDateTime createdAt;
        private LocalDateTime publishedAt;
        private LocalDateTime archivedAt;
        private ContentSummary contentSummary;
    }

    @Data
    @Builder
    public static class ContentSummary {
        private int sections;
        private int lectures;
        private int assignments;
        private int quizzes;
        private int totalDurationMinutes;
    }
}