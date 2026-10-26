package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.CourseProgressResponse;
import com.lms.courseservice.dto.LectureProgressRequest;
import com.lms.courseservice.dto.LectureProgressResponse;
import com.lms.courseservice.dto.ResumeLearningResponse;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final JwtUtil jwtUtil;

    private UUID extractUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    private String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    /**
     * Start a lecture
     */
    @PostMapping("/lectures/{lectureId}/start")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> startLecture(@PathVariable Long lectureId) {
        UUID studentId = extractUserId();
        LectureProgressResponse response = progressService.startLecture(studentId, lectureId);
        return ResponseEntity.ok(
                ApiResponse.<LectureProgressResponse>builder()
                        .success(true)
                        .message("Lecture started successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Update lecture progress
     */
    @PatchMapping("/lectures/{lectureId}")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> updateProgress(
            @PathVariable Long lectureId,
            @RequestBody LectureProgressRequest request) {
        UUID studentId = extractUserId();
        LectureProgressResponse response = progressService.updateProgress(studentId, lectureId, request);
        return ResponseEntity.ok(
                ApiResponse.<LectureProgressResponse>builder()
                        .success(true)
                        .message("Progress updated successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Complete a lecture
     */
    @PatchMapping("/lectures/{lectureId}/complete")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> completeLecture(@PathVariable Long lectureId) {
        UUID studentId = extractUserId();
        LectureProgressResponse response = progressService.completeLecture(studentId, lectureId);
        return ResponseEntity.ok(
                ApiResponse.<LectureProgressResponse>builder()
                        .success(true)
                        .message("Lecture completed successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get lecture progress
     */
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> getLectureProgress(@PathVariable Long lectureId) {
        UUID studentId = extractUserId();
        LectureProgressResponse response = progressService.getLectureProgress(studentId, lectureId);
        return ResponseEntity.ok(
                ApiResponse.<LectureProgressResponse>builder()
                        .success(true)
                        .message("Lecture progress retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get course progress
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseProgressResponse>> getCourseProgress(@PathVariable Long courseId) {
        UUID studentId = extractUserId();
        CourseProgressResponse response = progressService.getCourseProgress(studentId, courseId);
        return ResponseEntity.ok(
                ApiResponse.<CourseProgressResponse>builder()
                        .success(true)
                        .message("Course progress retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Resume learning - get where to continue
     */
    @GetMapping("/courses/{courseId}/resume")
    public ResponseEntity<ApiResponse<ResumeLearningResponse>> resumeLearning(@PathVariable Long courseId) {
        UUID studentId = extractUserId();
        ResumeLearningResponse response = progressService.resumeLearning(studentId, courseId);
        return ResponseEntity.ok(
                ApiResponse.<ResumeLearningResponse>builder()
                        .success(true)
                        .message("Resume learning information retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }
}
