package com.example.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single enrollment of a user in a course.
 * Used by Admin Service to enrich the user profile response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentInfoDTO {
    private Long courseId;
    private String courseName;
    private String category;
    private String status;
    private String activeTime; // ISO-8601 timestamp of the enrollment activity
}
