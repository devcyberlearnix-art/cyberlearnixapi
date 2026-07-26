package com.lms.paymentservice.service;

import com.lms.paymentservice.dto.CourseCreateRequest;
import com.lms.paymentservice.dto.CourseResponse;
import com.lms.paymentservice.entity.Course;
import com.lms.paymentservice.exception.BadRequestException;
import com.lms.paymentservice.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public CourseResponse createCourse(CourseCreateRequest request) {
        if (request.getInstructorName() == null || request.getInstructorName().isBlank()) {
            throw new BadRequestException("instructorName is required");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BadRequestException("amount must be greater than zero");
        }

        String courseId = (request.getCourseId() == null || request.getCourseId().isBlank())
                ? UUID.randomUUID().toString()
                : request.getCourseId().trim();

        if (courseRepository.existsByCourseId(courseId)) {
            throw new BadRequestException("courseId already exists: " + courseId);
        }

        String title = resolveTitle(courseId, request.getTitle());

        Course course = Course.builder()
                .courseId(courseId)
                .instructorName(request.getInstructorName().trim())
                .amount(request.getAmount())
                .title(title)
                .build();

        return toResponse(courseRepository.save(course));
    }

    public CourseResponse getCourse(String courseId) {
        return toResponse(findCourseOrThrow(courseId));
    }

    public List<CourseResponse> listCourses() {
        return courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Course findCourseOrThrow(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new BadRequestException("courseId is required");
        }
        return courseRepository.findByCourseId(courseId.trim())
                .orElseThrow(() -> new BadRequestException("Course not found: " + courseId.trim()));
    }

    public String resolveProductInfo(Course course) {
        return resolveTitle(course.getCourseId(), course.getTitle());
    }

    private String resolveTitle(String courseId, String title) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        return "Course: " + courseId;
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .instructorName(course.getInstructorName())
                .amount(course.getAmount())
                .title(course.getTitle())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
