package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    private DashboardOverview overview;
    private List<DashboardCourse> courses;
    private DashboardAssignments assignments;
    private DashboardQuizzes quizzes;
    private DashboardLiveClasses liveClasses;
    private List<DashboardCertificate> certificates;
    private List<DashboardRecentActivity> recentActivity;
}
