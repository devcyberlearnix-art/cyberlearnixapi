package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CourseResponseDTO {

    private Long courseId;
    private String title;
    private String description;
    private Double price;
    private String category;
    private String status;

    // 🔥 CHANGED TO UUID
    private UUID instructorId;

    private LocalDateTime createdAt;
}