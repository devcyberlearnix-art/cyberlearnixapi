package com.example.instructorservice.dto;

import com.example.instructorservice.entity.Course;
import lombok.Data;

import java.util.List;

@Data
public class CourseRequestDTO {
    private String title;
    private String description;
    private Double price;
    private String category;
    private Course.CourseStatus status; // ✅ ADD THIS
    private String subtitle;
    private List<String> tags;
    private String thumbnailUrl;
    private String previewVideoUrl;
}
