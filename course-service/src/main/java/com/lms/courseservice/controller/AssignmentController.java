package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.AssignmentRequest;
import com.lms.courseservice.dto.AssignmentResponse;
import com.lms.courseservice.dto.GradingRequest;
import com.lms.courseservice.dto.SubmissionRequest;
import com.lms.courseservice.dto.SubmissionResponse;
import com.lms.courseservice.dto.SubmissionResultResponse;
import com.lms.courseservice.service.AssignmentService;
import com.lms.courseservice.service.SubmissionService;
import com.lms.courseservice.repository.EnrollmentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final EnrollmentRepository enrollmentRepository;

    private boolean isStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            return "ROLE_INSTRUCTOR".equals(role) || "ROLE_MAIN_ADMIN".equals(role) || "ROLE_SUB_ADMIN".equals(role);
        });
    }

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
     * Create assignment
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(@RequestBody AssignmentRequest request) {
        UUID instructorId = extractUserId();
        AssignmentResponse response = assignmentService.createAssignment(request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<AssignmentResponse>builder()
                        .success(true)
                        .message("Assignment created successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Update assignment
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PutMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentRequest request) {
        UUID instructorId = extractUserId();
        AssignmentResponse response = assignmentService.updateAssignment(assignmentId, request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<AssignmentResponse>builder()
                        .success(true)
                        .message("Assignment updated successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get assignment by ID
     */
    @GetMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignment(@PathVariable Long assignmentId) {
        AssignmentResponse response = assignmentService.getAssignment(assignmentId);
        if (!isStaff()) {
            UUID studentId = extractUserId();
            if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, response.getCourseId())) {
                throw new AccessDeniedException("Student is not enrolled in this course. Course enrollment is required.");
            }
        }
        return ResponseEntity.ok(
                ApiResponse.<AssignmentResponse>builder()
                        .success(true)
                        .message("Assignment retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Delete assignment
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long assignmentId) {
        UUID instructorId = extractUserId();
        assignmentService.deleteAssignment(assignmentId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Assignment deleted successfully")
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Publish assignment
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{assignmentId}/publish")
    public ResponseEntity<ApiResponse<AssignmentResponse>> publishAssignment(@PathVariable Long assignmentId) {
        UUID instructorId = extractUserId();
        AssignmentResponse response = assignmentService.publishAssignment(assignmentId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<AssignmentResponse>builder()
                        .success(true)
                        .message("Assignment published successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Unpublish assignment
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{assignmentId}/unpublish")
    public ResponseEntity<ApiResponse<AssignmentResponse>> unpublishAssignment(@PathVariable Long assignmentId) {
        UUID instructorId = extractUserId();
        AssignmentResponse response = assignmentService.unpublishAssignment(assignmentId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<AssignmentResponse>builder()
                        .success(true)
                        .message("Assignment unpublished successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * List assignments for a course
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAssignmentsByCourse(@PathVariable Long courseId) {
        List<AssignmentResponse> response = assignmentService.getAssignmentsByCourse(courseId);
        return ResponseEntity.ok(
                ApiResponse.<List<AssignmentResponse>>builder()
                        .success(true)
                        .message("Assignments retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * List assignments for a section
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAssignmentsBySection(@PathVariable Long sectionId) {
        List<AssignmentResponse> response = assignmentService.getAssignmentsBySection(sectionId);
        return ResponseEntity.ok(
                ApiResponse.<List<AssignmentResponse>>builder()
                        .success(true)
                        .message("Assignments retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== STUDENT APIs ====================

    /**
     * Get published assignments for a course (student view)
     */
    @GetMapping("/course/{courseId}/published")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getPublishedAssignmentsByCourse(@PathVariable Long courseId) {
        if (!isStaff()) {
            UUID studentId = extractUserId();
            if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
                throw new AccessDeniedException("Student is not enrolled in this course. Course enrollment is required.");
            }
        }
        List<AssignmentResponse> response = assignmentService.getPublishedAssignmentsByCourse(courseId);
        return ResponseEntity.ok(
                ApiResponse.<List<AssignmentResponse>>builder()
                        .success(true)
                        .message("Published assignments retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Create or update submission (draft)
     */
    @PostMapping("/{assignmentId}/submission")
    public ResponseEntity<ApiResponse<SubmissionResponse>> createOrUpdateSubmission(
            @PathVariable Long assignmentId,
            @RequestBody SubmissionRequest request) {
        UUID studentId = extractUserId();
        SubmissionResponse response = submissionService.createOrUpdateSubmission(assignmentId, studentId, request);
        return ResponseEntity.ok(
                ApiResponse.<SubmissionResponse>builder()
                        .success(true)
                        .message("Submission saved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Submit assignment
     */
    @PostMapping("/{assignmentId}/submission/submit")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssignment(@PathVariable Long assignmentId) {
        UUID studentId = extractUserId();
        SubmissionResponse response = submissionService.submitAssignment(assignmentId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<SubmissionResponse>builder()
                        .success(true)
                        .message("Assignment submitted successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get own submission for an assignment
     */
    @GetMapping("/{assignmentId}/submission")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmission(@PathVariable Long assignmentId) {
        UUID studentId = extractUserId();
        SubmissionResponse response = submissionService.getSubmission(assignmentId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<SubmissionResponse>builder()
                        .success(true)
                        .message("Submission retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get submission result
     */
    @GetMapping("/{assignmentId}/result")
    public ResponseEntity<ApiResponse<SubmissionResultResponse>> getSubmissionResult(@PathVariable Long assignmentId) {
        UUID studentId = extractUserId();
        SubmissionResultResponse response = submissionService.getSubmissionResult(assignmentId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<SubmissionResultResponse>builder()
                        .success(true)
                        .message("Submission result retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get all submissions for a student
     */
    @GetMapping("/student/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsByStudent() {
        UUID studentId = extractUserId();
        List<SubmissionResponse> response = submissionService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(
                ApiResponse.<List<SubmissionResponse>>builder()
                        .success(true)
                        .message("Submissions retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== GRADING APIs ====================

    /**
     * Get all submissions for an assignment (instructor view)
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        List<SubmissionResponse> response = submissionService.getSubmissionsByAssignment(assignmentId);
        return ResponseEntity.ok(
                ApiResponse.<List<SubmissionResponse>>builder()
                        .success(true)
                        .message("Submissions retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get individual submission by ID
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(@PathVariable Long submissionId) {
        SubmissionResponse response = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(
                ApiResponse.<SubmissionResponse>builder()
                        .success(true)
                        .message("Submission retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Grade submission
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<ApiResponse<SubmissionResponse>> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody GradingRequest request) {
        UUID instructorId = extractUserId();
        SubmissionResponse response = submissionService.gradeSubmission(submissionId, request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<SubmissionResponse>builder()
                        .success(true)
                        .message("Submission graded successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }
}