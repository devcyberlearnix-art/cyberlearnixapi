package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.DashboardCourseDetailResponse;
import com.lms.courseservice.dto.DashboardCourseResponse;
import com.lms.courseservice.dto.StudentDashboardResponse;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    private UUID extractUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    private String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    /**
     * Get student dashboard overview
     * GET /api/v1/dashboard/student
     * Authentication: STUDENT, USER
     */
    @GetMapping("/student")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard() {
        UUID studentId = extractUserId();
        StudentDashboardResponse response = dashboardService.getStudentDashboard(studentId);
        return ResponseEntity.ok(
                ApiResponse.<StudentDashboardResponse>builder()
                        .success(true)
                        .message("Student dashboard retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get student's enrolled courses with progress
     * GET /api/v1/dashboard/student/courses
     * Authentication: STUDENT, USER
     */
    @GetMapping("/student/courses")
    public ResponseEntity<ApiResponse<DashboardCourseResponse>> getStudentCourses() {
        UUID studentId = extractUserId();
        DashboardCourseResponse response = dashboardService.getStudentCourses(studentId);
        return ResponseEntity.ok(
                ApiResponse.<DashboardCourseResponse>builder()
                        .success(true)
                        .message("Student courses retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get course-specific dashboard for a student
     * GET /api/v1/dashboard/student/courses/{courseId}
     * Authentication: STUDENT, USER
     */
    @GetMapping("/student/courses/{courseId}")
    public ResponseEntity<ApiResponse<DashboardCourseDetailResponse>> getCourseDashboard(
            @PathVariable Long courseId) {
        UUID studentId = extractUserId();
        DashboardCourseDetailResponse response = dashboardService.getCourseDashboard(studentId, courseId);
        return ResponseEntity.ok(
                ApiResponse.<DashboardCourseDetailResponse>builder()
                        .success(true)
                        .message("Course dashboard retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }
}
