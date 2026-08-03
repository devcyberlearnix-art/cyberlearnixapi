package com.example.instructorservice.controller;

import com.example.instructorservice.dto.*;
import com.example.instructorservice.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/{id}/courses")
    public ResponseEntity<ApiResponse<CourseFullResponseDTO>> createCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseRequestDTO request
    ) {

        CourseFullResponseDTO response =
                courseService.createCourse(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CourseFullResponseDTO>builder()
                        .success(true)
                        .message("Course created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getCoursesByInstructor(
            @PathVariable("id") UUID instructorId
    ) {

        List<CourseResponseDTO> courses =
                courseService.getCoursesByInstructor(instructorId);

        return ResponseEntity.ok(
                ApiResponse.<List<CourseResponseDTO>>builder()
                        .success(true)
                        .message("Courses fetched successfully")
                        .data(courses)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/{id}/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseById(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {

        CourseResponseDTO course =
                courseService.getCourseById(instructorId, courseId);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponseDTO>builder()
                        .success(true)
                        .message("Course fetched successfully")
                        .data(course)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PutMapping("/{id}/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> updateCourse(
            @PathVariable("id")UUID instructorId,
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequestDTO request
    ) {

        CourseResponseDTO updated =
                courseService.updateCourse(instructorId, courseId, request);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponseDTO>builder()
                        .success(true)
                        .message("Course updated successfully")
                        .data(updated)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @DeleteMapping("/{id}/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> deleteCourse(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {

        CourseResponseDTO deletedCourse =
                courseService.deleteCourse(instructorId, courseId);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponseDTO>builder()
                        .success(true)
                        .message("Course unpublished successfully")
                        .data(deletedCourse)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    @GetMapping("/{id}/courses/{courseId}/students")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> getEnrolledStudents(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        StudentResponseDTO response = courseService.getEnrolledStudents(instructorId, courseId);

        return ResponseEntity.ok(
                ApiResponse.<StudentResponseDTO>builder()
                        .success(true)
                        .message("Enrolled students fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    @GetMapping("/{id}/courses/{courseId}/students/{studentId}")
    public ResponseEntity<ApiResponse<StudentProgressResponseDTO>> getStudentProgress(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @PathVariable UUID studentId
    ) {
        StudentProgressResponseDTO response = courseService.getStudentProgress(instructorId, courseId, studentId);

        return ResponseEntity.ok(
                ApiResponse.<StudentProgressResponseDTO>builder()
                        .success(true)
                        .message("Student progress fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    @PostMapping("/{id}/courses/{courseId}/grades")
    public ResponseEntity<ApiResponse<GradeResponseDTO>> assignOrUpdateGrade(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId,
            @RequestBody GradeRequestDTO request
    ) {
        GradeResponseDTO gradeResponse = courseService.assignOrUpdateGrade(instructorId, courseId, request);

        return ResponseEntity.ok(
                ApiResponse.<GradeResponseDTO>builder()
                        .success(true)
                        .message("Grade assigned/updated successfully")
                        .data(gradeResponse)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    @GetMapping("/{id}/courses/{courseId}/analytics")
    public ResponseEntity<ApiResponse<CourseFullResponseDTO>> getCourseAnalytics(
            @PathVariable("id") UUID instructorId,
            @PathVariable Long courseId
    ) {
        CourseFullResponseDTO response = courseService.getCourseAnalytics(instructorId, courseId);

        return ResponseEntity.ok(
                ApiResponse.<CourseFullResponseDTO>builder()
                        .success(true)
                        .message("Course analytics fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}