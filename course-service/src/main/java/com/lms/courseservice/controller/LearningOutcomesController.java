package com.lms.courseservice.controller;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.LearningOutcomesDTO;
import com.lms.courseservice.dto.LearningOutcomesResponse;
import com.lms.courseservice.service.LearningOutcomesService;
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
public class LearningOutcomesController {

    private final LearningOutcomesService learningOutcomesService;
    private final AuditLogger auditLogger;

    /**
     * Get all learning outcomes for a course (Public)
     */
    @GetMapping("/{courseId}/outcomes")
    public ResponseEntity<List<LearningOutcomesDTO>> getOutcomes(@PathVariable Long courseId) {
        List<LearningOutcomesDTO> outcomes = learningOutcomesService.getOutcomesByCourseId(courseId);
        return ResponseEntity.ok(outcomes);
    }

    /**
     * Create a learning outcome (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping("/{courseId}/outcomes")
    public ResponseEntity<LearningOutcomesResponse> createOutcome(
            @PathVariable Long courseId,
            @Valid @RequestBody LearningOutcomesDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        LearningOutcomesResponse response = learningOutcomesService.createOutcome(courseId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a learning outcome (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PutMapping("/outcomes/{outcomeId}")
    public ResponseEntity<LearningOutcomesResponse> updateOutcome(
            @PathVariable Long outcomeId,
            @Valid @RequestBody LearningOutcomesDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        LearningOutcomesResponse response = learningOutcomesService.updateOutcome(outcomeId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Partially update a learning outcome (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/outcomes/{outcomeId}")
    public ResponseEntity<LearningOutcomesResponse> updateOutcomePartial(
            @PathVariable Long outcomeId,
            @RequestBody LearningOutcomesDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        LearningOutcomesResponse response = learningOutcomesService.updateOutcome(outcomeId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a learning outcome (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @DeleteMapping("/outcomes/{outcomeId}")
    public ResponseEntity<LearningOutcomesResponse> deleteOutcome(
            @PathVariable Long outcomeId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        LearningOutcomesResponse response = learningOutcomesService.deleteOutcome(outcomeId, userId, ipAddress);
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