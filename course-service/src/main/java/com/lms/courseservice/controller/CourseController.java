package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;

import com.lms.courseservice.dto.CourseInfo;
import com.lms.courseservice.dto.CourseListResponse;
import com.lms.courseservice.dto.DeleteCourseResponse;
import com.lms.courseservice.dto.EnrollCourseResponse;
import com.lms.courseservice.dto.EnrollmentInfo;
import com.lms.courseservice.dto.EnrolledStudentInfo;
import com.lms.courseservice.dto.EnrolledStudentsResponse;

import com.lms.courseservice.dto.FeaturedCourseResponse;
import com.lms.courseservice.dto.TrendingCoursesResponse;
import com.lms.courseservice.dto.TrendingResponseData;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final JwtUtil jwtUtil;

    /**
     * Create Course (Instructor/Admin only - enforced by SecurityConfig)
     */
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseService.createCourse(course);
    }

    /**
     * Get All Courses (Public)
     */
    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    /**
     * Get Course List with Filters (Public - Phase 1)
     * Returns published courses with optional filtering
     */
    @GetMapping("/list")
    public ResponseEntity<CourseListResponse> getCourseList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean premium,
            @RequestParam(required = false) Boolean free,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            CourseListResponse response = courseService.getCourseList(
                search, category, level, language, minPrice, maxPrice, premium, free, paid, sort, page, size);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            CourseListResponse errorResponse = CourseListResponse.builder()
                .success(false)
                .message(e.getMessage())
                .data(null)
                .timestamp(java.time.Instant.now().toString())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/stats")
    public Map<String, Object> getCourseStats() {
        return courseService.getCourseStats();
    }

    @GetMapping("/featured")
    public List<FeaturedCourseResponse> getFeaturedCourses(@RequestParam(defaultValue = "6") int limit) {
        return courseService.getFeaturedCourses(limit);
    }

    /**
     * Get Trending Courses (Public - No Authentication Required)
     * Returns paginated trending courses sorted by calculated trending score
     */
    @GetMapping("/trending")
    public ResponseEntity<TrendingCoursesResponse> getTrendingCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level) {
        try {
            TrendingCoursesResponse response = courseService.getTrendingCourses(page, size, category, level);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            TrendingCoursesResponse errorResponse = TrendingCoursesResponse.builder()
                .success(false)
                .message(e.getMessage())
                .data(null)
                .timestamp(java.time.Instant.now().toString())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{courseId}/impressions")
    public void trackCourseImpression(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "HOME") String source) {
        courseService.trackImpression(courseId, source);
    }

    /**
     * Get Course by ID (Public)
     */
    @GetMapping("/{id}")
    public Course getCourse(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    /**
     * Update Course - Full (Instructor/Admin only)
     */
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course course) {
        return courseService.updateCourse(id, course);
    }

    /**
     * Update Course - Partial (Instructor/Admin only)
     */
    @PatchMapping("/{id}")
    public Course updateCoursePartial(@PathVariable Long id, @RequestBody Course course) {
        return courseService.updateCourse(id, course);
    }

    /**
     * Update Course Status (Admin service only - for approval/rejection)
     * Accepts a simple JSON with status field
     */
    @PatchMapping("/{id}/status")
    public Course updateCourseStatus(@PathVariable Long id, @RequestBody Course statusUpdate) {
        Course existingCourse = courseService.getCourseById(id);
        if (existingCourse != null && statusUpdate.getStatus() != null) {
            existingCourse.setStatus(statusUpdate.getStatus());
            return courseService.updateCourse(id, existingCourse);
        }
        return existingCourse;
    }

    /**
     * Delete Course (Instructor/Admin only)
     */
    @DeleteMapping("/{id}")
    public DeleteCourseResponse deleteCourse(@PathVariable Long id) {
        // Retrieve the course before deletion to include its details in the response
        Course course = courseService.getCourseById(id);
        // Perform deletion
        courseService.deleteCourse(id);
        // Build the data payload
        CourseInfo courseInfo = new CourseInfo(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                String.valueOf(course.getInstructorId()), // Placeholder for actual instructor name if needed
                true // deleted flag
        );
        return new DeleteCourseResponse(true,
                "Course deleted successfully",
                courseInfo);
    }

    /**
     * Get students enrolled in course (Public)
     */
    @GetMapping("/{courseId}/students")
    public EnrolledStudentsResponse getStudents(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId);
        java.util.List<EnrolledStudentInfo> studentList = courseService.getEnrolledStudentDetails(courseId);

        EnrolledStudentsResponse.EnrolledStudentsData data =
                new EnrolledStudentsResponse.EnrolledStudentsData(
                        course.getId(),
                        course.getTitle(),
                        studentList.size(),
                        studentList
                );

        return new EnrolledStudentsResponse(true,
                "Enrolled students fetched successfully",
                data);
    }

    /**
     * Enroll user in course (Student only - enforced by SecurityConfig)
     * Token/User extracted from SecurityContext by JwtFilter
     */
    @PostMapping("/{courseId}/enroll")
    public EnrollCourseResponse enroll(@PathVariable Long courseId) {
        UUID userId = extractUserIdFromContext();
        courseService.enrollFreeCourse(courseId, userId);
        
        Course course = courseService.getCourseById(courseId);
        EnrollmentInfo info = new EnrollmentInfo(
                course.getId(),
                course.getTitle(),
                userId,
                course.getCategory(),
                "Enrolled",
                java.time.LocalDateTime.now().toString()
        );
        
        return new EnrollCourseResponse(true,
            "Student enrolled in the course successfully.",
            info);
    }

    /**
     * Extract userId from Spring Security context (set by JwtFilter)
     */
    private UUID extractUserIdFromContext() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }
}