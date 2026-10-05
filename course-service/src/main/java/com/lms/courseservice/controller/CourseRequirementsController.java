package com.lms.courseservice.controller;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.CourseRequirementsDTO;
import com.lms.courseservice.dto.CourseRequirementsResponse;
import com.lms.courseservice.service.CourseRequirementsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseRequirementsController {

    private final CourseRequirementsService courseRequirementsService;
    private final AuditLogger auditLogger;

    /**
     * Get all requirements for a course (Public)
     */
    @GetMapping("/{courseId}/requirements")
    public ResponseEntity<List<CourseRequirementsDTO>> getRequirements(@PathVariable Long courseId) {
        List<CourseRequirementsDTO> requirements = courseRequirementsService.getRequirementsByCourseId(courseId);
        return ResponseEntity.ok(requirements);
    }

    /**
     * Create a course requirement (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping("/{courseId}/requirements")
    public ResponseEntity<CourseRequirementsResponse> createRequirement(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequirementsDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {

        UUID userId = extractUserIdFromContext();
        CourseRequirementsResponse response = courseRequirementsService.createRequirement(courseId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a course requirement (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PutMapping("/requirements/{requirementId}")
    public ResponseEntity<CourseRequirementsResponse> updateRequirement(
            @PathVariable Long requirementId,
            @Valid @RequestBody CourseRequirementsDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {

        UUID userId = extractUserIdFromContext();
        CourseRequirementsResponse response = courseRequirementsService.updateRequirement(requirementId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Partially update a course requirement (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/requirements/{requirementId}")
    public ResponseEntity<CourseRequirementsResponse> updateRequirementPartial(
            @PathVariable Long requirementId,
            @RequestBody CourseRequirementsDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {

        UUID userId = extractUserIdFromContext();
        CourseRequirementsResponse response = courseRequirementsService.updateRequirement(requirementId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a course requirement (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @DeleteMapping("/requirements/{requirementId}")
    public ResponseEntity<CourseRequirementsResponse> deleteRequirement(
            @PathVariable Long requirementId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {

        UUID userId = extractUserIdFromContext();
        CourseRequirementsResponse response = courseRequirementsService.deleteRequirement(requirementId, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Extract userId from Spring Security context (set by JwtFilter)
     */
    private UUID extractUserIdFromContext() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }
}
