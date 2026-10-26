package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResultResponse {
    private Long questionId;
    private String questionText;
    private Integer marks;
    private Integer marksObtained;
    private Boolean isCorrect;
}
