package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCourse {
    private Long courseId;
    private String courseTitle;
    private Double progressPercentage;
    private Integer completedLectures;
    private Integer totalLectures;
    private String status;
    private Long lastLectureId;
    private Long resumeLectureId;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
}
