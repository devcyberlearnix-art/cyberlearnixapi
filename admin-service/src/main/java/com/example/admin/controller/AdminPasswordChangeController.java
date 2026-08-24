package com.example.admin.controller;

import com.example.admin.dto.AdminPasswordChangeDto;
import com.example.admin.dto.AdminPasswordChangeResponse;
import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.VerifyAdminPasswordOtpDto;
import com.example.admin.security.AdminPrincipal;
import com.example.admin.service.AdminPasswordChangeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller exposing the high-security password change feature for admins.
 * Unified endpoints matching the specs:
 * - POST /api/v1/admin/change-password
 * - POST /api/v1/admin/change-password/verify-otp
 */
@RestController
@RequestMapping("/api/v1/admin/change-password")
@RequiredArgsConstructor
@Slf4j
public class AdminPasswordChangeController {

    private final AdminPasswordChangeService adminPasswordChangeService;

    /**
     * Initiates the admin password change flow.
     * Re-authenticates old password, checks reuse history, and generates/sends OTP.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AdminPasswordChangeResponse>> initiatePasswordChange(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AdminPasswordChangeDto dto) {

        log.info("[AdminPasswordChangeController] POST /api/v1/admin/change-password — initiating admin password change");
        
        UUID adminId = getAuthenticatedAdminId();
        AdminPasswordChangeResponse response = adminPasswordChangeService.initiatePasswordChange(httpRequest, adminId, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Password change request initiated. Please verify the OTP sent to your email within 5 minutes.",
                response,
                LocalDateTime.now().toString()));
    }

    /**
     * Verifies the admin password change OTP and updates the password.
     * Revokes the active admin session and invalidates all active tokens.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AdminPasswordChangeResponse>> verifyPasswordOtp(
            HttpServletRequest httpRequest,
            @Valid @RequestBody VerifyAdminPasswordOtpDto dto) {

        log.info("[AdminPasswordChangeController] POST /api/v1/admin/change-password/verify-otp — verifying OTP");

        UUID adminId = getAuthenticatedAdminId();
        AdminPasswordChangeResponse response = adminPasswordChangeService.verifyPasswordOtp(httpRequest, adminId, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Password successfully changed. All active sessions have been invalidated.",
                response,
                LocalDateTime.now().toString()));
    }

    private UUID getAuthenticatedAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated admin access.");
        }
        if (auth.getPrincipal() instanceof AdminPrincipal principal) {
            return principal.getAdminId();
        }
        throw new RuntimeException("Unexpected principal in SecurityContext. Expected AdminPrincipal.");
    }
}
