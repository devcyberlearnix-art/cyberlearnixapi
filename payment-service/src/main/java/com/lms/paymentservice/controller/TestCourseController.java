package com.lms.paymentservice.controller;

import com.lms.paymentservice.dto.CourseCreateRequest;
import com.lms.paymentservice.dto.CourseResponse;
import com.lms.paymentservice.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Test-only course catalog inside payment-service.
 * Create a course here, then pass courseId to POST /payments/create to auto-fill PayU amount.
 */
@RestController
@RequestMapping("/test/courses")
public class TestCourseController {

    private final CourseService courseService;

    public TestCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseCreateRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> listCourses() {
        return ResponseEntity.ok(courseService.listCourses());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(courseService.getCourse(courseId));
    }
}
