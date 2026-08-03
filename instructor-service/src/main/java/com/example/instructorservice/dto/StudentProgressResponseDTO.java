package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class StudentProgressResponseDTO {

    private boolean success;
    private String message;
    private String requestId;

    private StudentProgressData data;

    @Data
    @Builder
    public static class StudentProgressData {
        private UUID studentId;
        private Long courseId;
        private String status;               // ENROLLED, COMPLETED, DROPPED, etc.
        private Double completionRate;       // 0.0 to 100.0
        private LocalDateTime enrolledAt;
        private LocalDateTime lastActivityAt;
    }
}