package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {
    private Long courseId;
    private Long sectionId;
    private String title;
    private String description;
    private String instructions;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private Integer passingScore;
}
