package com.lms.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponse {
    private String courseId;
    private String instructorName;
    private Double amount;
    private String title;
    private LocalDateTime createdAt;
}
