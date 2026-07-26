package com.example.instructorservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleResponse {

    // Module Details
    private UUID moduleId;
    private String moduleTitle;
    private String moduleDescription;
    private String moduleStatus; // e.g., ACTIVE, INACTIVE
    private Integer moduleOrder;
    private LocalDateTime moduleCreatedAt;
    private LocalDateTime moduleUpdatedAt;

    // Course Details
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseStatus; // DRAFT, PUBLISHED, ARCHIVED
    private LocalDateTime courseCreatedAt;
    private Integer totalModules;

    // Instructor Details
    private UUID instructorId;
    private String instructorName;
    private String instructorEmail;

    // API Metadata
    private String status; // "success" or "error"
    private String message;
    private String requestId;
    private LocalDateTime timestamp;
}
