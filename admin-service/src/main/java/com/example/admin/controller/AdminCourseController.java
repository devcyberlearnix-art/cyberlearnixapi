package com.example.admin.controller;

import com.example.admin.client.AdminCourseServiceClient;
import com.example.admin.client.AdminCourseServiceClient.CourseDTO;
import com.example.admin.dto.CourseListResponse;
import com.example.admin.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin Course Management Controller
 * Integrates with Course Service (Port 8083) via AdminCourseServiceClient
 * All courseId parameters are Long (from Course Service)
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminCourseController {

    private final CourseService courseService;

    public AdminCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Get all courses from Course Service
     */
    @GetMapping("/courses")
    public CourseListResponse getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        List<Object> dataList = new ArrayList<>(courses);

        return CourseListResponse.builder()
                .success(true)
                .message("Courses fetched successfully")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Get course by ID (Long)
     */
    @GetMapping("/courses/{courseId}")
    public CourseListResponse getCourseById(@PathVariable Long courseId) {
        CourseDTO course = courseService.getCourseById(courseId);
        List<Object> dataList = course != null ? List.of(course) : List.of();

        return CourseListResponse.builder()
                .success(course != null)
                .message(course != null ? "Course fetched successfully" : "Course not found")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @GetMapping("/content/{courseId}")
    public CourseListResponse getCourseContent(@PathVariable Long courseId) {
        List<Object> content = courseService.getCourseContent(courseId);
        return CourseListResponse.builder()
                .success(true)
                .message("Course content fetched successfully")
                .data(content)
                .count(content.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Approve course (update status to Published)
     */
    @PutMapping("/courses/{courseId}/approve")
    public CourseListResponse approveCourse(@PathVariable Long courseId) {
        try {
            CourseDTO approvedCourse = courseService.approveCourse(courseId);
            List<Object> dataList = approvedCourse != null ? List.of(approvedCourse) : List.of();

            return CourseListResponse.builder()
                    .success(approvedCourse != null)
                    .message(approvedCourse != null ? "Course approved successfully" : "Failed to approve course")
                    .data(dataList)
                    .count(dataList.size())
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        } catch (RuntimeException ex) {
            return CourseListResponse.builder()
                    .success(false)
                    .message("Failed to approve course: " + ex.getMessage())
                    .data(List.of())
                    .count(0)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }
    }

    /**
     * Reject course (update status to Archived)
     */
    @PutMapping("/courses/{courseId}/reject")
    public CourseListResponse rejectCourse(@PathVariable Long courseId) {
        CourseDTO rejectedCourse = courseService.rejectCourse(courseId);
        List<Object> dataList = rejectedCourse != null ? List.of(rejectedCourse) : List.of();

        return CourseListResponse.builder()
                .success(rejectedCourse != null)
                .message(rejectedCourse != null ? "Course rejected successfully" : "Failed to reject course")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Delete course
     */
    @DeleteMapping("/courses/{courseId}")
    public CourseListResponse deleteCourse(@PathVariable Long courseId) {
        boolean deleted = courseService.deleteCourse(courseId);
        List<Object> dataList = List.of();

        return CourseListResponse.builder()
                .success(deleted)
                .message(deleted ? "Course deleted successfully" : "Failed to delete course")
                .data(dataList)
                .count(0)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // --- Section endpoints ---
    @PostMapping("/courses/{courseId}/sections")
    public CourseListResponse createSection(@PathVariable Long courseId, @RequestBody Map<String, Object> sectionPayload) {
        Map result = courseService.createSection(courseId, sectionPayload);
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Section created" : "Failed to create section")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @DeleteMapping("/sections/{sectionId}")
    public CourseListResponse deleteSection(@PathVariable Long sectionId) {
        boolean ok = courseService.deleteSection(sectionId);
        return CourseListResponse.builder()
                .success(ok)
                .message(ok ? "Section deleted" : "Failed to delete section")
                .data(List.of())
                .count(0)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // --- Lecture endpoints ---
    @PostMapping("/sections/{sectionId}/lectures")
    public CourseListResponse createLecture(@PathVariable Long sectionId, @RequestBody Map<String, Object> lecturePayload) {
        Map result = courseService.createLecture(sectionId, lecturePayload);
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Lecture created" : "Failed to create lecture")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @PutMapping("/sections/{sectionId}/lectures/{lectureId}/approve")
    public CourseListResponse approveLecture(@PathVariable Long sectionId, @PathVariable Long lectureId) {
        Map result = courseService.updateLecturePreview(sectionId, lectureId, true);
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Lecture approved" : "Failed to approve lecture")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @PutMapping("/sections/{sectionId}/lectures/{lectureId}/reject")
    public CourseListResponse rejectLecture(@PathVariable Long sectionId, @PathVariable Long lectureId) {
        Map result = courseService.updateLecturePreview(sectionId, lectureId, false);
        List<Object> dataList = result != null ? List.of(result) : List.of();
        return CourseListResponse.builder()
                .success(result != null)
                .message(result != null ? "Lecture rejected" : "Failed to reject lecture")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @DeleteMapping("/sections/{sectionId}/lectures/{lectureId}")
    public CourseListResponse deleteLecture(@PathVariable Long sectionId, @PathVariable Long lectureId) {
        boolean ok = courseService.deleteLecture(sectionId, lectureId);
        return CourseListResponse.builder()
                .success(ok)
                .message(ok ? "Lecture deleted" : "Failed to delete lecture")
                .data(List.of())
                .count(0)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Get courses by instructor ID (Long)
     */
    @GetMapping("/instructors/{instructorId}/courses")
    public CourseListResponse getCoursesByInstructor(@PathVariable Long instructorId) {
        List<CourseDTO> courses = courseService.getCoursesByInstructor(instructorId);
        List<Object> dataList = new ArrayList<>(courses);

        return CourseListResponse.builder()
                .success(true)
                .message("Courses fetched successfully")
                .data(dataList)
                .count(dataList.size())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}