package com.lms.courseservice.controller;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.CourseDetailsDTO;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.CourseDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseDetailsController {

    private final CourseDetailsService courseDetailsService;
    private final JwtUtil jwtUtil;
    private final AuditLogger auditLogger;

    /**
     * Get comprehensive course details (Public - optional auth for enrollment status)
     * Aggregates course info, instructor profile, curriculum, reviews, enrollment status, requirements, outcomes, materials, FAQ
     */
    @GetMapping("/{courseId}/details")
    public ResponseEntity<ApiResponse<CourseDetailsDTO>> getCourseDetails(
            @PathVariable Long courseId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        try {
            UUID userId = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                try {
                    userId = jwtUtil.extractUserId(authorization.substring(7));
                } catch (Exception e) {
                    // Invalid token, continue without user context
                }
            }

            CourseDetailsDTO courseDetails = courseDetailsService.getCourseDetails(courseId, userId);

            // Log audit event
            auditLogger.logSuccess("COURSE_DETAILS_VIEW",
                    userId != null ? userId.toString() : "anonymous",
                    "course-service",
                    "GET",
                    "/api/v1/courses/" + courseId + "/details",
                    "Course details fetched for course ID: " + courseId,
                    ipAddress);

            return ResponseEntity.ok(
                    ApiResponse.<CourseDetailsDTO>builder()
                            .success(true)
                            .message("Course details fetched successfully")
                            .data(courseDetails)
                            .timestamp(Instant.now().toString())
                            .build()
            );
        } catch (RuntimeException e) {
            auditLogger.logFailure("COURSE_DETAILS_VIEW",
                    "anonymous",
                    "course-service",
                    "GET",
                    "/api/v1/courses/" + courseId + "/details",
                    "Failed to fetch course details: " + e.getMessage(),
                    ipAddress);

            return ResponseEntity.status(404)
                    .body(ApiResponse.<CourseDetailsDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .timestamp(Instant.now().toString())
                            .build());
        }
    }
}
