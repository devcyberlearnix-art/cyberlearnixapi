package com.example.instructorservice.dto;

import com.example.instructorservice.entity.Course;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CourseFullResponseDTO {

    private boolean success;
    private String message;
    private String requestId;

    // ❌ OLD: private Data data;
    // ✅ FIXED:
    private CourseData data;

    @Data
    @Builder
    public static class CourseData {

        private Identity identity;
        private Core core;
        private Status status;
        private InstructorDTO instructor;
        private Content content;
        private Media media;
        private Analytics analytics;
        private Visibility visibility;
        private Timestamps timestamps;
        private Links links;
        @Singular("studentProgressData") // <-- ADD THIS
        private List<StudentProgressResponseDTO.StudentProgressData> studentProgress;

    }

    @Data @Builder
    public static class Identity {
        private Long courseId;
        private String slug;

    }

    @Data @Builder
    public static class Core {
        private String title;
        private String subtitle;
        private String description;
        private String language;
        private String level;
        private String category;
        private List<String> tags;
    }

    @Data @Builder
    public static class Status {
        private Course.CourseStatus status;
        private boolean isPublished;
        private boolean isArchived;
        private String visibility;
        private String approvalStatus;
        private String publishedBy;
    }

    @Data @Builder
    public static class InstructorDTO {
        private UUID instructorId;
        private String name;
        private String headline;
        private double rating;
        private boolean verified;
        private String email;       // ✅ ADD THIS

    }

    @Data @Builder
    public static class Content {
        private int sections;
        private int lectures;
        private int totalDurationMinutes;
        private int assignments;
        private int quizzes;
    }

    @Data @Builder
    public static class Media {
        private String thumbnailUrl;
        private String previewVideoUrl;
    }

    @Data @Builder
    public static class Analytics {
        private int enrolledStudents;
        private int activeLearners;
        private double completionRate;
        private double averageRating;
        private int totalReviews;
        private double totalRevenue;
    }

    @Data @Builder
    public static class Visibility {
        private boolean isPublic;
        private boolean isSearchable;
        private boolean allowPreview;
    }

    @Data @Builder
    public static class Timestamps {
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime publishedAt;
        private LocalDateTime archivedAt;
    }

    @Data @Builder
    public static class Links {
        private String self;
        private String enroll;
        private String reviews;
        private String content;
    }
}