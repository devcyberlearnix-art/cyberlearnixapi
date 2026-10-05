package com.lms.courseservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequirementsDTO {
    private Long id;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Requirement type is required")
    @Size(max = 50, message = "Requirement type must not exceed 50 characters")
    private String requirementType;

    @NotBlank(message = "Requirement text is required")
    private String requirementText;
}
