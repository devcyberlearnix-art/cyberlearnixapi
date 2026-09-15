package com.user.register.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorProfileDTO {
    private UUID instructorId;
    private String name;
    private String email;
    private String headline;
    private String bio;
    private List<String> expertise;
    private Double rating;
    private Integer totalCourses;
    private Integer totalStudents;
    private Boolean verified;
}