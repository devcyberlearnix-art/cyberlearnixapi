package com.example.instructorservice.controller;

import com.example.instructorservice.dto.ApiResponse;
import com.example.instructorservice.dto.CourseResponseDTO;
import com.example.instructorservice.exeception.NotFoundException;
import com.example.instructorservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class PublicCourseController {

    private final CourseService courseService;

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseById(
            @PathVariable Long courseId) {
        CourseResponseDTO course = courseService.getCourseById(courseId);
        return ResponseEntity.ok(ApiResponse.success("Course fetched successfully", course));
    }
}
