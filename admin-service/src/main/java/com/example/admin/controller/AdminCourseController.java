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
        if (course == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Course not found");
        }

        return CourseListResponse.builder()
                .success(true)
                .message("Course fetched successfully")
                .data(List.of(course))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @GetMapping("/content/{courseId}")
    public CourseListResponse getCourseContent(@PathVariable Long courseId) {
        CourseDTO course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Course not found");
        }
        List<Object> content = courseService.getCourseContent(courseId);
        return CourseListResponse.builder()
                .success(true)
                .message("Course content fetched successfully")
                .data(content)
                .count(content != null ? content.size() : 0)
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
            if (approvedCourse == null) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Course not found");
            }

            return CourseListResponse.builder()
                    .success(true)
                    .message("Course approved successfully")
                    .data(List.of(approvedCourse))
                    .count(1)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Failed to approve course: " + ex.getMessage());
        }
    }

    /**
     * Reject course (update status to Archived)
     */
    @PutMapping("/courses/{courseId}/reject")
    public CourseListResponse rejectCourse(@PathVariable Long courseId) {
        CourseDTO rejectedCourse = courseService.rejectCourse(courseId);
        if (rejectedCourse == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Course not found");
        }

        return CourseListResponse.builder()
                .success(true)
                .message("Course rejected successfully")
                .data(List.of(rejectedCourse))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Delete course
     */
    @DeleteMapping("/courses/{courseId}")
    public CourseListResponse deleteCourse(@PathVariable Long courseId) {
        Map result = courseService.deleteCourse(courseId);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Course not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Course deleted successfully")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // --- Section endpoints ---
    @PostMapping("/courses/{courseId}/sections")
    public CourseListResponse createSection(@PathVariable Long courseId, @RequestBody Map<String, Object> sectionPayload) {
        if (sectionPayload == null || sectionPayload.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Section payload is required");
        }
        Map result = courseService.createSection(courseId, sectionPayload);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Course not found or failed to create section");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Section created")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @DeleteMapping("/sections/{sectionId}")
    public CourseListResponse deleteSection(@PathVariable Long sectionId) {
        Map result = courseService.deleteSection(sectionId);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Section not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Section deleted")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // --- Lecture endpoints ---
    @PostMapping("/sections/{sectionId}/lectures")
    public CourseListResponse createLecture(@PathVariable Long sectionId, @RequestBody Map<String, Object> lecturePayload) {
        if (lecturePayload == null || lecturePayload.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Lecture payload is required");
        }
        Map result = courseService.createLecture(sectionId, lecturePayload);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Section not found or failed to create lecture");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Lecture created")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @PutMapping("/sections/{sectionId}/lectures/{lectureId}/approve")
    public CourseListResponse approveLecture(@PathVariable Long sectionId, @PathVariable Long lectureId) {
        Map result = courseService.updateLecturePreview(sectionId, lectureId, true);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Lecture not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Lecture approved")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @PutMapping("/sections/{sectionId}/lectures/{lectureId}/reject")
    public CourseListResponse rejectLecture(@PathVariable Long sectionId, @PathVariable Long lectureId) {
        Map result = courseService.updateLecturePreview(sectionId, lectureId, false);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Lecture not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Lecture rejected")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @DeleteMapping("/sections/{sectionId}/lectures/{lectureId}")
    public CourseListResponse deleteLecture(@PathVariable Long sectionId, @PathVariable Long lectureId) {
        Map result = courseService.deleteLecture(sectionId, lectureId);
        if (result == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Lecture not found");
        }
        return CourseListResponse.builder()
                .success(true)
                .message("Lecture deleted")
                .data(List.of(result))
                .count(1)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Get courses by instructor ID (Long)
     */
    @GetMapping("/instructors/{instructorId}/courses")
    public CourseListResponse getCoursesByInstructor(@PathVariable String instructorId) {
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
