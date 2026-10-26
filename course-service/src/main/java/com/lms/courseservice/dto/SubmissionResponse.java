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
public class SubmissionResponse {
    private Long id;
    private Long assignmentId;
    private UUID studentId;
    private String submissionText;
    private String fileUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private Integer score;
    private String feedback;
    private UUID gradedBy;
    private LocalDateTime gradedAt;
    private Integer attemptNumber;
}