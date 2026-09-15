package com.example.instructorservice.dto;

import com.example.instructorservice.entity.Course;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CourseRequestDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be at least 0.0")
    private Double price;
    
    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
    
    private Course.CourseStatus status; // ✅ ADD THIS
    
    @Size(max = 200, message = "Subtitle must not exceed 200 characters")
    private String subtitle;
    
    private List<String> tags;
    
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "Thumbnail URL must be a valid URL")
    private String thumbnailUrl;
    
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "Preview video URL must be a valid URL")
    private String previewVideoUrl;
    
    @Size(max = 50, message = "Level must not exceed 50 characters")
    private String level;
    
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;
}
