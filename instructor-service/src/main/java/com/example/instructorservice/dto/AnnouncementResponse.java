package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AnnouncementResponse {

    private UUID announcementId;
    private Long courseId;
    private UUID instructorId;

    private String title;
    private String message;

    private LocalDateTime createdAt;

    private String status;
}