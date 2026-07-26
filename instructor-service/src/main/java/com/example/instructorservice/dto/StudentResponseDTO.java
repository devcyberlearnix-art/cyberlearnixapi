package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class StudentResponseDTO {

    private boolean success;
    private String message;
    private String requestId;
    private List<StudentData> data;

    @Data
    @Builder
    public static class StudentData {
        private UUID studentId;
        private String name;
        private String email;
        private String status; // e.g., ACTIVE, COMPLETED
        private LocalDateTime enrolledAt;
        private LocalDateTime lastActivityAt;
    }
}
