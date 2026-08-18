package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.PasswordChangeDto;
import com.user.register.dto.PasswordChangeResponse;
import com.user.register.dto.VerifyPasswordOtpDto;
import com.user.register.service.PasswordChangeService;
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
 * Controller exposing the high-security password change feature for users.
 * Unified endpoints matching:
 * - POST /users/change-password
 * - POST /users/change-password/verify-otp
 *
 * Mapped to /api/v1/users/change-password
 */
@RestController
@RequestMapping("/api/v1/users/change-password")
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeController {

    private final PasswordChangeService passwordChangeService;

    /**
     * Initiates the password change flow.
     * Re-authenticates old password, checks reuse history, and generates/sends OTP.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PasswordChangeResponse>> initiatePasswordChange(
            HttpServletRequest httpRequest,
            @Valid @RequestBody PasswordChangeDto dto) {

        log.info("[PasswordChangeController] POST /api/v1/users/change-password — initiating password change");
        
        UUID userId = getAuthenticatedUserId();
        PasswordChangeResponse response = passwordChangeService.initiatePasswordChange(httpRequest, userId, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Password change request initiated. Please verify the OTP sent to your email within 5 minutes.",
                response,
                LocalDateTime.now()));
    }

    /**
     * Verifies the password change OTP and updates the password.
     * Revokes all active user sessions on success.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<PasswordChangeResponse>> verifyPasswordOtp(
            HttpServletRequest httpRequest,
            @Valid @RequestBody VerifyPasswordOtpDto dto) {

        log.info("[PasswordChangeController] POST /api/v1/users/change-password/verify-otp — verifying OTP");

        UUID userId = getAuthenticatedUserId();
        PasswordChangeResponse response = passwordChangeService.verifyPasswordOtp(httpRequest, userId, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Password successfully changed. All active sessions have been invalidated.",
                response,
                LocalDateTime.now()));
    }

    private UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated access.");
        }
        try {
            return UUID.fromString((String) auth.getPrincipal());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid principal in SecurityContext. Must be a valid UUID.");
        }
    }
}
