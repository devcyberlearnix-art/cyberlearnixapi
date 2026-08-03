package com.user.register.controller;

import com.user.register.dto.unified.*;
import com.user.register.service.UnifiedAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UnifiedAuthenticationController {

    private final UnifiedAuthenticationService unifiedAuthenticationService;

    /**
     * Unified Login Endpoint
     * Authenticates: Student, Instructor, Admin, Super Admin
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return unifiedAuthenticationService.login(request, httpRequest);
    }

    /**
     * Refresh Token Endpoint
     * POST /api/v1/auth/refresh
     * Refresh token is extracted from Authorization header as Bearer token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        // Extract refresh token from Authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    RefreshTokenResponse.builder()
                            .success(false)
                            .message("Authorization header with Bearer token is required")
                            .timestamp(java.time.LocalDateTime.now())
                            .build()
            );
        }

        String refreshToken = authorizationHeader.substring(7);
        
        return unifiedAuthenticationService.refreshToken(refreshToken);
    }

    /**
     * Logout Endpoint
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        String accessToken = null;
        String refreshToken = null;

        // Extract access token from Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }

        // Extract refresh token from request body
        if (requestBody != null) {
            refreshToken = requestBody.get("refreshToken");
        }

        return unifiedAuthenticationService.logout(accessToken, refreshToken);
    }

    /**
     * Request Login OTP
     * POST /api/v1/auth/login/otp/request
     */
    @PostMapping("/login/otp/request")
    public ResponseEntity<Map<String, Object>> requestLoginOtp(
            @Valid @RequestBody RequestOtpRequest request) {
        return unifiedAuthenticationService.requestLoginOtp(request);
    }

    /**
     * Verify Login OTP
     * POST /api/v1/auth/login/otp/verify
     */
    @PostMapping("/login/otp/verify")
    public ResponseEntity<LoginResponse> verifyLoginOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest) {
        return unifiedAuthenticationService.verifyLoginOtp(request, httpRequest);
    }

    /**
     * Forgot Password
     * POST /api/v1/auth/password/forgot
     */
    @PostMapping("/password/forgot")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        return unifiedAuthenticationService.forgotPassword(request);
    }

    /**
     * Verify OTP for Password Reset
     * POST /api/v1/auth/password/verify-otp
     */
    @PostMapping("/password/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyPasswordOtp(
            @RequestBody VerifyOtpRequest request) {
        return unifiedAuthenticationService.verifyPasswordOtp(request);
    }

    /**
     * Reset Password
     * POST /api/v1/auth/password/reset
     */
    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestBody ResetPasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Extract email from JWT token if available, otherwise use email from request body
        String email = request.getEmail();
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String tokenEmail = unifiedAuthenticationService.extractEmailFromToken(token);
                if (tokenEmail != null) {
                    email = tokenEmail;
                }
            } catch (Exception e) {
                // If token extraction fails, use email from request body
            }
        }
        return unifiedAuthenticationService.resetPassword(request, email);
    }

    /**
     * Change Password
     * POST /api/v1/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        // Extract access token from Authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "Authorization header is required",
                    "timestamp", java.time.LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String accessToken = authorizationHeader.substring(7);
        
        // Extract email from token
        String email = null;
        try {
            email = unifiedAuthenticationService.extractEmailFromToken(accessToken);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "Invalid access token",
                    "timestamp", java.time.LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Delegate to service
        return unifiedAuthenticationService.changePassword(request, email);
    }

    /**
     * Switch Role
     * POST /api/v1/auth/switch-role
     */
    @PostMapping("/switch-role")
    public ResponseEntity<Map<String, Object>> switchRole(
            @RequestBody com.user.register.dto.SwitchRoleRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        // Extract access token from Authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "Authorization header is required",
                    "timestamp", java.time.LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String accessToken = authorizationHeader.substring(7);
        
        // Extract email from token
        String email = null;
        try {
            email = unifiedAuthenticationService.extractEmailFromToken(accessToken);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "Invalid access token",
                    "timestamp", java.time.LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Delegate to service
        return unifiedAuthenticationService.switchRole(request, email);
    }
}
