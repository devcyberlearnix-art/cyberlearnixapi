package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.CourseContentResponse;
import com.lms.courseservice.dto.LectureDetailResponse;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.CourseContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseContentController {

    private final CourseContentService courseContentService;
    private final JwtUtil jwtUtil;

    /**
     * Get course content for enrolled students
     * GET /api/v1/courses/{courseId}/content
     */
    @GetMapping("/{courseId}/content")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<CourseContentResponse>> getCourseContent(
            @PathVariable Long courseId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        UUID studentId = extractStudentId(authorization);

        CourseContentResponse content = courseContentService.getCourseContent(studentId, courseId);

        return ResponseEntity.ok(
                ApiResponse.<CourseContentResponse>builder()
                        .success(true)
                        .message("Course content retrieved successfully")
                        .data(content)
                        .timestamp(Instant.now().toString())
                        .build()
        );
    }

    /**
     * Get lecture detail for enrolled students
     * GET /api/v1/lectures/{lectureId}/learn
     */
    @GetMapping("/lectures/{lectureId}/learn")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<LectureDetailResponse>> getLectureDetail(
            @PathVariable Long lectureId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        UUID studentId = extractStudentId(authorization);

        LectureDetailResponse detail = courseContentService.getLectureDetail(studentId, lectureId);

        return ResponseEntity.ok(
                ApiResponse.<LectureDetailResponse>builder()
                        .success(true)
                        .message("Lecture detail retrieved successfully")
                        .data(detail)
                        .timestamp(Instant.now().toString())
                        .build()
        );
    }

    private UUID extractStudentId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // For preview lectures, we might allow anonymous access
            // This will be validated in the service layer
            return null;
        }

        try {
            String token = authorization.substring(7);
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token");
        }
    }
}
