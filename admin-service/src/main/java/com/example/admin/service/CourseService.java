package com.example.admin.service;

import com.example.admin.client.AdminCourseServiceClient;
import com.example.admin.client.AdminCourseServiceClient.CourseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Admin Service - Course Management
 * Integrates with Course Service (Port 8083)
 * No direct database access - all queries via REST
 * 
 * ✅ CHANGES:
 * - Removed direct database access to instructor DB
 * - Now uses AdminCourseServiceClient for all operations
 * - Single source of truth: Course Service
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final AdminCourseServiceClient courseServiceClient;

    /**
     * Get all courses from Course Service
     */
    public List<CourseDTO> getAllCourses() {
        return courseServiceClient.getAllCourses();
    }

    /**
     * Get course by ID
     */
    public CourseDTO getCourseById(Long courseId) {
        return courseServiceClient.getCourseById(courseId);
    }

    /**
     * Approve course (update status to Published)
     */
    public CourseDTO approveCourse(Long courseId) {
        return courseServiceClient.updateCourseStatus(courseId, "Published");
    }

    /**
     * Reject course (update status to Archived)
     */
    public CourseDTO rejectCourse(Long courseId) {
        return courseServiceClient.updateCourseStatus(courseId, "Archived");
    }

    /**
     * Delete course
     */
    public boolean deleteCourse(Long courseId) {
        return courseServiceClient.deleteCourse(courseId);
    }

    /**
     * Get courses by instructor
     */
    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
        return courseServiceClient.getCoursesByInstructor(instructorId);
    }

    public List<Object> getCourseContent(Long courseId) {
        return courseServiceClient.getCourseContent(courseId);
    }

    // Section & Lecture management
    public Map createSection(Long courseId, Map<String, Object> sectionPayload) {
        return courseServiceClient.createSection(courseId, sectionPayload);
    }

    public boolean deleteSection(Long sectionId) {
        return courseServiceClient.deleteSection(sectionId);
    }

    public Map createLecture(Long sectionId, Map<String, Object> lecturePayload) {
        return courseServiceClient.createLecture(sectionId, lecturePayload);
    }

    public Map updateLecturePreview(Long sectionId, Long lectureId, boolean previewEnabled) {
        return courseServiceClient.updateLecturePreview(sectionId, lectureId, previewEnabled);
    }

    public boolean deleteLecture(Long sectionId, Long lectureId) {
        return courseServiceClient.deleteLecture(sectionId, lectureId);
    }
}
