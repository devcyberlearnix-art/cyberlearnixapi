package com.lms.courseservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 200, message = "Subtitle must not exceed 200 characters")
    private String subtitle;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Size(max = 50, message = "Level must not exceed 50 characters")
    private String level;

    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be at least 0.0")
    private BigDecimal price;

    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "Thumbnail must be a valid URL")
    private String thumbnail;

    @NotNull(message = "Instructor ID is required")
    private UUID instructorId;

    @Pattern(regexp = "DRAFT|PUBLISHED|ARCHIVED", message = "Status must be DRAFT, PUBLISHED, or ARCHIVED")
    private String status;

    private Boolean premium;
}
