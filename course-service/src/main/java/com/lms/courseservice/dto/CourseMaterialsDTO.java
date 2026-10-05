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
public class CourseMaterialsDTO {
    private Long id;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Material name is required")
    @Size(max = 255, message = "Material name must not exceed 255 characters")
    private String materialName;

    @NotBlank(message = "Material type is required")
    @Size(max = 50, message = "Material type must not exceed 50 characters")
    private String materialType;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    private Long fileSize;
}
