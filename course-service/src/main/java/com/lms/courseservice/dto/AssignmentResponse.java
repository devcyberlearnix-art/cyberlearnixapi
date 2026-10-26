package com.lms.courseservice.dto;

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
public class AssignmentResponse {
    private Long id;
    private Long courseId;
    private Long sectionId;
    private String title;
    private String description;
    private String instructions;
    private Integer maxScore;
    private LocalDateTime dueDate;
    private String status;
    private String allowedFileTypes;
    private Long maxFileSizeBytes;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}