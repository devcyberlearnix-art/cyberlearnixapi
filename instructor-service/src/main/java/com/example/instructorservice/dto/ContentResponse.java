package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContentResponse {

    private UUID contentId;
    private String contentTitle;
    private String contentType; // VIDEO / PDF / QUIZ

    private Long courseId;
    private String instructorId;

    private String status; // PUBLISHED / DRAFT

    private String message;
    private String requestId;
    private LocalDateTime timestamp;
}
