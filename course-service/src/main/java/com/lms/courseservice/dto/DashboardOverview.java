package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverview {
    private Integer totalEnrolledCourses;
    private Integer completedCourses;
    private Integer inProgressCourses;
    private Integer totalCertificates;
    private Double averageQuizScore;
    private Double overallAttendancePercentage;
}
