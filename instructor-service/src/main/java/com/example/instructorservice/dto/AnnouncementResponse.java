package com.example.instructorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {

    // Announcement Details
    private UUID announcementId;
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private String status; // "PUBLISHED"

    // Course Details
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseStatus;
    private LocalDateTime courseCreatedAt;

    // Instructor Details
    private UUID instructorId;
    private String instructorName;
    private String instructorEmail;

    // Metadata
    private String requestId;
    private LocalDateTime timestamp;
}