package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.CourseInfo;
import com.lms.courseservice.dto.DeleteCourseResponse;
import com.lms.courseservice.dto.EnrollCourseResponse;
import com.lms.courseservice.dto.EnrollmentInfo;
import com.lms.courseservice.dto.EnrolledStudentInfo;
import com.lms.courseservice.dto.EnrolledStudentsResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
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
                "Enrolled"
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