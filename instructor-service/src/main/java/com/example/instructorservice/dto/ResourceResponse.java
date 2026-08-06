package com.example.instructorservice.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResourceResponse {

    // Resource
    private UUID contentId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime uploadedAt;

    // Course
    private Long courseId;
    private String courseTitle;

    // Instructor
    private UUID instructorId;
    private String instructorName;
    private String instructorEmail;

    // Meta
    private String status;
    private String message;
    private String requestId;
    private LocalDateTime timestamp;
}
