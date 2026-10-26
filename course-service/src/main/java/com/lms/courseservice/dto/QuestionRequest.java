package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    private String questionText;
    private String questionType;
    private Integer marks;
    private Integer orderIndex;
    private String explanation;
    private List<OptionRequest> options;
}
