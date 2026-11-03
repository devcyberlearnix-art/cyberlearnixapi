package com.lms.courseservice.security;

import com.lms.courseservice.entity.Course;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@Slf4j
public class CourseSecurityService {

    public void validateCourseOwnership(Course course) {
        if (course == null) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
            a.getAuthority().equals("ROLE_MAIN_ADMIN") ||
            a.getAuthority().equals("ROLE_SUB_ADMIN") ||
            a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return; // Admins have full platform management privileges
        }

        Object principal = auth.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal.toString())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        UUID callerId = principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
        if (course.getInstructorId() != null && !course.getInstructorId().equals(callerId)) {
            log.warn("Security check failed: Caller {} attempted unauthorized modification of course {} owned by instructor {}",
                callerId, course.getId(), course.getInstructorId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Access denied: You do not have ownership permission for this course.");
        }
    }

    public void validateCourseStatusTransition(Course course, String newStatus) {
        if (newStatus == null) return;
        if ("PUBLISHED".equalsIgnoreCase(newStatus.trim())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_MAIN_ADMIN") ||
                    a.getAuthority().equals("ROLE_SUB_ADMIN") ||
                    a.getAuthority().equals("ROLE_ADMIN"));
                if (!isAdmin) {
                    log.warn("Security check failed: Non-admin attempted direct course publication for course {}", course.getId());
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Publishing a course requires administrative review and approval. Instructors can submit courses for review with status PENDING_REVIEW.");
                }
            }
        }
    }

    public UUID getAuthenticatedCallerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal.toString())) return null;
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    public boolean isCallerAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
            a.getAuthority().equals("ROLE_MAIN_ADMIN") ||
            a.getAuthority().equals("ROLE_SUB_ADMIN") ||
            a.getAuthority().equals("ROLE_ADMIN"));
    }
}

