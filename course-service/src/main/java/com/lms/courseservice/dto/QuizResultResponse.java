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
public class QuizResultResponse {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private Long attemptId;
    private Integer attemptNumber;
    private Integer score;
    private Integer totalMarks;
    private Integer percentage;
    private Boolean passed;
    private LocalDateTime submittedAt;
    private List<QuestionResultResponse> questionResults;
}
