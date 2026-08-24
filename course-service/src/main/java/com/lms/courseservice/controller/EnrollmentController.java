package com.lms.courseservice.controller;

import com.lms.courseservice.dto.EnrollCourseResponse;
import com.lms.courseservice.dto.EnrollmentInfo;
import com.lms.courseservice.dto.EnrollmentCheckDetailResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final JwtUtil jwtUtil;

    public record EnrollmentRequest(Long courseId, UUID userId) {}

    @PostMapping("/internal/enroll")
    public EnrollCourseResponse enrollStudentInternal(@RequestBody EnrollmentRequest request) {
        courseService.enrollAfterPayment(request.courseId(), request.userId());

        Course course = courseService.getCourseById(request.courseId());

        EnrollmentInfo info = new EnrollmentInfo(
                course.getId(),
                course.getTitle(),
                request.userId(),
                course.getCategory(),
                "Enrolled",
                java.time.LocalDateTime.now().toString()
        );

        return new EnrollCourseResponse(
                true,
                "Student enrolled in the course successfully after payment.",
                info
        );
    }

    @GetMapping("/check/{courseId}")
    public EnrollmentCheckDetailResponse checkEnrollment(@PathVariable Long courseId) {
        UUID userId = extractUserIdFromContext();
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(userId, courseId);

        Course course = courseService.getCourseById(courseId);
        String status = enrolled ? "Enrolled" : "Not Enrolled";

        EnrollmentCheckDetailResponse.EnrollmentCheckData data =
                new EnrollmentCheckDetailResponse.EnrollmentCheckData(
                        course.getId(),
                        course.getTitle(),
                        userId,
                        enrolled,
                        status
                );

        return new EnrollmentCheckDetailResponse(
                true,
                enrolled ? "Student is enrolled in this course." : "Student is not enrolled in this course.",
                data
            );
    }

    private UUID extractUserIdFromContext() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    @GetMapping("/users/{userId}")
    public List<EnrollmentInfo> getEnrollmentsByUserId(@PathVariable UUID userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(userId);
        return enrollments.stream()
                .map(e -> {
                    String courseTitle = "Unknown Course";
                    String category = "Unknown Category";
                    try {
                        Course course = courseService.getCourseById(e.getCourseId());
                        if (course != null) {
                            courseTitle = course.getTitle();
                            category = course.getCategory();
                        }
                    } catch (Exception ex) {
                        // Fallback in case course is deleted or not found
                    }
                    return new EnrollmentInfo(
                            e.getCourseId(),
                            courseTitle,
                            e.getStudentId(),
                            category,
                            e.getStatus() != null ? e.getStatus() : "ACTIVE",
                            e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : java.time.LocalDateTime.now().toString()
                    );
                })
                .toList();
    }
}
