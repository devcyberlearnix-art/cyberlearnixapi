package com.lms.courseservice.controller;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.CourseFAQDTO;
import com.lms.courseservice.dto.CourseFAQResponse;
import com.lms.courseservice.service.CourseFAQService;
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
public class CourseFAQController {

    private final CourseFAQService courseFAQService;
    private final AuditLogger auditLogger;

    /**
     * Get all FAQs for a course (Public)
     */
    @GetMapping("/{courseId}/faqs")
    public ResponseEntity<List<CourseFAQDTO>> getFAQs(@PathVariable Long courseId) {
        List<CourseFAQDTO> faqs = courseFAQService.getFAQsByCourseId(courseId);
        return ResponseEntity.ok(faqs);
    }

    /**
     * Create a course FAQ (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping("/{courseId}/faqs")
    public ResponseEntity<CourseFAQResponse> createFAQ(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseFAQDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        CourseFAQResponse response = courseFAQService.createFAQ(courseId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a course FAQ (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PutMapping("/faqs/{faqId}")
    public ResponseEntity<CourseFAQResponse> updateFAQ(
            @PathVariable Long faqId,
            @Valid @RequestBody CourseFAQDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        CourseFAQResponse response = courseFAQService.updateFAQ(faqId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Partially update a course FAQ (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/faqs/{faqId}")
    public ResponseEntity<CourseFAQResponse> updateFAQPartial(
            @PathVariable Long faqId,
            @RequestBody CourseFAQDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        CourseFAQResponse response = courseFAQService.updateFAQ(faqId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a course FAQ (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @DeleteMapping("/faqs/{faqId}")
    public ResponseEntity<CourseFAQResponse> deleteFAQ(
            @PathVariable Long faqId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        CourseFAQResponse response = courseFAQService.deleteFAQ(faqId, userId, ipAddress);
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