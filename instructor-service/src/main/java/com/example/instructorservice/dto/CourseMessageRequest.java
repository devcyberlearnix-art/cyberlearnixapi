package com.example.instructorservice.dto;

import lombok.Data;

@Data
public class CourseMessageRequest {
    private String subject;
    private String message;
}
