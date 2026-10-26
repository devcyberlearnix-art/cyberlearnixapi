package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.CertificateResponse;
import com.lms.courseservice.dto.CertificateVerificationResponse;
import com.lms.courseservice.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    private UUID extractUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    private String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    // ==================== STUDENT APIs ====================

    /**
     * Generate or get certificate for a completed course
     */
    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'INSTRUCTOR', 'MAIN_ADMIN', 'SUB_ADMIN')")
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCourseCertificate(@PathVariable Long courseId) {
        UUID studentId = extractUserId();
        CertificateResponse response = certificateService.generateCertificate(studentId, courseId);
        return ResponseEntity.ok(
                ApiResponse.<CertificateResponse>builder()
                        .success(true)
                        .message("Certificate retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get certificate by ID
     */
    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'INSTRUCTOR', 'MAIN_ADMIN', 'SUB_ADMIN')")
    @GetMapping("/{certificateId}")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificateById(@PathVariable Long certificateId) {
        UUID studentId = extractUserId();
        CertificateResponse response = certificateService.getCertificateById(certificateId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<CertificateResponse>builder()
                        .success(true)
                        .message("Certificate retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get all certificates for the authenticated student
     */
    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'INSTRUCTOR', 'MAIN_ADMIN', 'SUB_ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getMyCertificates() {
        UUID studentId = extractUserId();
        List<CertificateResponse> response = certificateService.getMyCertificates(studentId);
        return ResponseEntity.ok(
                ApiResponse.<List<CertificateResponse>>builder()
                        .success(true)
                        .message("Certificates retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== PUBLIC VERIFICATION API ====================

    /**
     * Verify certificate by verification code (public endpoint)
     */
    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<ApiResponse<CertificateVerificationResponse>> verifyCertificate(
            @PathVariable String verificationCode) {
        CertificateVerificationResponse response = certificateService.verifyCertificate(verificationCode);
        if (response != null && !response.isValid() && "Certificate not found".equals(response.getMessage())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Certificate not found");
        }
        return ResponseEntity.ok(
                ApiResponse.<CertificateVerificationResponse>builder()
                        .success(response.isValid())
                        .message(response.getMessage())
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== ADMIN APIs ====================

    /**
     * Get all certificates for a course
     */
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'SUB_ADMIN')")
    @GetMapping("/admin/courses/{courseId}")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getCertificatesByCourse(
            @PathVariable Long courseId) {
        List<CertificateResponse> response = certificateService.getCertificatesByCourse(courseId);
        return ResponseEntity.ok(
                ApiResponse.<List<CertificateResponse>>builder()
                        .success(true)
                        .message("Certificates retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Revoke a certificate
     */
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'SUB_ADMIN')")
    @PatchMapping("/admin/{certificateId}/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeCertificate(
            @PathVariable Long certificateId,
            @RequestBody(required = false) RevokeRequest revokeRequest) {
        UUID adminId = extractUserId();
        String reason = revokeRequest != null ? revokeRequest.getReason() : "No reason provided";
        certificateService.revokeCertificate(certificateId, adminId, reason);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Certificate revoked successfully")
                        .data(null)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // Inner class for revoke request
    public static class RevokeRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
