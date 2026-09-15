package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.CourseDetailsDTO;
import com.lms.courseservice.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    /**
     * Get hierarchical course curriculum structure (Public)
     * Returns course → sections → lectures hierarchy with durations and preview status
     */
    @GetMapping("/{courseId}/curriculum")
    public ResponseEntity<ApiResponse<CourseDetailsDTO.CourseCurriculumDTO>> getCourseCurriculum(
            @PathVariable Long courseId) {
        try {
            CourseDetailsDTO.CourseCurriculumDTO curriculum = curriculumService.getCourseCurriculum(courseId);
            return ResponseEntity.ok(
                    ApiResponse.<CourseDetailsDTO.CourseCurriculumDTO>builder()
                            .success(true)
                            .message("Course curriculum fetched successfully")
                            .data(curriculum)
                            .timestamp(Instant.now().toString())
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.<CourseDetailsDTO.CourseCurriculumDTO>builder()
                            .success(false)
                            .message(e.getMessage())
                            .data(null)
                            .timestamp(Instant.now().toString())
                            .build());
        }
    }
}