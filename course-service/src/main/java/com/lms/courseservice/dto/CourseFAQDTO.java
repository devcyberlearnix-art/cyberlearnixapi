package com.lms.courseservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseFAQDTO {
    private Long id;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Answer is required")
    private String answer;

    private Integer displayOrder;
}
