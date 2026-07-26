package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CourseEarningDTO {

    private Long courseId;
    private String title;
    private double price;
    private long enrollments;
    private double revenue;
}
