package com.lms.courseservice.controller;

import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final JwtUtil jwtUtil;

    public record EnrollmentRequest(Long courseId, UUID userId) {}
    public record EnrollmentCheckResponse(boolean enrolled) {}

    /**
     * Internal enrollment endpoint (Service-to-Service)
     * Called by payment service when payment is successful
     */
    @PostMapping("/internal/enroll")
    public void enrollStudentInternal(@RequestBody EnrollmentRequest request) {
        courseService.enrollAfterPayment(request.courseId(), request.userId());
    }

    /**
     * Check if user is enrolled in course
     * Used by review service to verify enrollment before allowing reviews
     * User info extracted from SecurityContext by JwtFilter
     */
    @GetMapping("/check/{courseId}")
    public EnrollmentCheckResponse checkEnrollment(@PathVariable Long courseId) {
        UUID userId = extractUserIdFromContext();
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(userId, courseId);
        return new EnrollmentCheckResponse(enrolled);
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
