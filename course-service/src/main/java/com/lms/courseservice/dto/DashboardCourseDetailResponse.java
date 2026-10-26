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
public class DashboardCourseDetailResponse {
    private Long courseId;
    private String courseTitle;
    private Double progressPercentage;
    private Integer completedLectures;
    private Integer totalLectures;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
    private Long lastLectureId;
    private Long resumeLectureId;
    private String lastLectureTitle;
    private String resumeLectureTitle;
    private DashboardAssignments assignments;
    private DashboardQuizzes quizzes;
    private DashboardLiveClasses liveClasses;
    private DashboardCertificate certificate;
}
