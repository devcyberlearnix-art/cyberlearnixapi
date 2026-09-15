package com.lms.courseservice.controller;

import com.cyberlearnix.audit.AuditLogger;
import com.lms.courseservice.dto.CourseMaterialsDTO;
import com.lms.courseservice.dto.CourseMaterialsResponse;
import com.lms.courseservice.service.CourseMaterialsService;
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
public class CourseMaterialsController {

    private final CourseMaterialsService courseMaterialsService;
    private final AuditLogger auditLogger;

    /**
     * Get all course materials for a course (Public)
     */
    @GetMapping("/{courseId}/materials")
    public ResponseEntity<List<CourseMaterialsDTO>> getMaterials(@PathVariable Long courseId) {
        List<CourseMaterialsDTO> materials = courseMaterialsService.getMaterialsByCourseId(courseId);
        return ResponseEntity.ok(materials);
    }

    /**
     * Create a course material (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN','ADMIN')")
    @PostMapping("/{courseId}/materials")
    public ResponseEntity<CourseMaterialsResponse> createMaterial(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseMaterialsDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        CourseMaterialsResponse response = courseMaterialsService.createMaterial(courseId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a course material (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN','ADMIN')")
    @PutMapping("/materials/{materialId}")
    public ResponseEntity<CourseMaterialsResponse> updateMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody CourseMaterialsDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        CourseMaterialsResponse response = courseMaterialsService.updateMaterial(materialId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Partially update a course material (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN','ADMIN')")
    @PatchMapping("/materials/{materialId}")
    public ResponseEntity<CourseMaterialsResponse> updateMaterialPartial(
            @PathVariable Long materialId,
            @RequestBody CourseMaterialsDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        CourseMaterialsResponse response = courseMaterialsService.updateMaterial(materialId, dto, userId, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a course material (Instructor/Admin only)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN','ADMIN')")
    @DeleteMapping("/materials/{materialId}")
    public ResponseEntity<CourseMaterialsResponse> deleteMaterial(
            @PathVariable Long materialId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        UUID userId = extractUserIdFromContext();
        CourseMaterialsResponse response = courseMaterialsService.deleteMaterial(materialId, userId, ipAddress);
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