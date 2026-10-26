package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.LiveClassJoinResponse;
import com.lms.courseservice.dto.LiveClassRequest;
import com.lms.courseservice.dto.LiveClassResponse;
import com.lms.courseservice.service.LiveClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/live-classes")
@RequiredArgsConstructor
public class LiveClassController {

    private final LiveClassService liveClassService;

    private UUID extractUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    private String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    // ==================== INSTRUCTOR/ADMIN APIs ====================

    /**
     * Create a new live class
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<LiveClassResponse>> createLiveClass(@RequestBody LiveClassRequest request) {
        UUID instructorId = extractUserId();
        LiveClassResponse response = liveClassService.createLiveClass(request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<LiveClassResponse>builder()
                        .success(true)
                        .message("Live class created successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Update an existing live class
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PutMapping("/{liveClassId}")
    public ResponseEntity<ApiResponse<LiveClassResponse>> updateLiveClass(
            @PathVariable Long liveClassId,
            @RequestBody LiveClassRequest request) {
        UUID instructorId = extractUserId();
        LiveClassResponse response = liveClassService.updateLiveClass(liveClassId, request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<LiveClassResponse>builder()
                        .success(true)
                        .message("Live class updated successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get live class by ID
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/{liveClassId}")
    public ResponseEntity<ApiResponse<LiveClassResponse>> getLiveClassById(@PathVariable Long liveClassId) {
        UUID instructorId = extractUserId();
        LiveClassResponse response = liveClassService.getLiveClassById(liveClassId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<LiveClassResponse>builder()
                        .success(true)
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get all live classes for a course
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<LiveClassResponse>>> getLiveClassesByCourse(@PathVariable Long courseId) {
        UUID instructorId = extractUserId();
        List<LiveClassResponse> response = liveClassService.getLiveClassesByCourse(courseId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<List<LiveClassResponse>>builder()
                        .success(true)
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get all live classes for a section
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<LiveClassResponse>>> getLiveClassesBySection(@PathVariable Long sectionId) {
        UUID instructorId = extractUserId();
        List<LiveClassResponse> response = liveClassService.getLiveClassesBySection(sectionId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<List<LiveClassResponse>>builder()
                        .success(true)
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get upcoming live classes for a course
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN','STUDENT','USER')")
    @GetMapping("/course/{courseId}/upcoming")
    public ResponseEntity<ApiResponse<List<LiveClassResponse>>> getUpcomingLiveClassesByCourse(@PathVariable Long courseId) {
        UUID userId = extractUserId();
        List<LiveClassResponse> response = liveClassService.getUpcomingLiveClassesForStudent(courseId, userId);
        return ResponseEntity.ok(
                ApiResponse.<List<LiveClassResponse>>builder()
                        .success(true)
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Start a live class
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{liveClassId}/start")
    public ResponseEntity<ApiResponse<Void>> startLiveClass(@PathVariable Long liveClassId) {
        UUID instructorId = extractUserId();
        liveClassService.startLiveClass(liveClassId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Live class started successfully")
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Complete a live class
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{liveClassId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeLiveClass(
            @PathVariable Long liveClassId,
            @RequestBody(required = false) LiveClassCompleteRequest request) {
        UUID instructorId = extractUserId();
        String recordingUrl = request != null ? request.getRecordingUrl() : null;
        liveClassService.completeLiveClass(liveClassId, instructorId, recordingUrl);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Live class completed successfully")
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Cancel a live class
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{liveClassId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelLiveClass(@PathVariable Long liveClassId) {
        UUID instructorId = extractUserId();
        liveClassService.cancelLiveClass(liveClassId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Live class cancelled successfully")
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Delete a live class
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @DeleteMapping("/{liveClassId}")
    public ResponseEntity<ApiResponse<Void>> deleteLiveClass(@PathVariable Long liveClassId) {
        UUID instructorId = extractUserId();
        liveClassService.deleteLiveClass(liveClassId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Live class deleted successfully")
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== STUDENT APIs ====================

    /**
     * Join a live class
     */
    @PreAuthorize("hasAnyRole('STUDENT','USER','INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/{liveClassId}/join")
    public ResponseEntity<ApiResponse<LiveClassJoinResponse>> joinLiveClass(@PathVariable Long liveClassId) {
        UUID studentId = extractUserId();
        LiveClassJoinResponse response = liveClassService.joinLiveClass(liveClassId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<LiveClassJoinResponse>builder()
                        .success(true)
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== DTOs ====================

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LiveClassCompleteRequest {
        private String recordingUrl;
    }
}
