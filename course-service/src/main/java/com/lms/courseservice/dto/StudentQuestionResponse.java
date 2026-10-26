package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuestionResponse {
    private Long id;
    private Long quizId;
    private String questionText;
    private String questionType;
    private Integer marks;
    private Integer orderIndex;
    private String explanation;
    private List<StudentOptionResponse> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
