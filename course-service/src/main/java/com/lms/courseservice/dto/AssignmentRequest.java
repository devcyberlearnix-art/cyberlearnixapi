package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    private Long courseId;
    private Long sectionId;
    private String title;
    private String description;
    private String instructions;
    private Integer maxScore;
    private LocalDateTime dueDate;
    private String allowedFileTypes;
    private Long maxFileSizeBytes;
}