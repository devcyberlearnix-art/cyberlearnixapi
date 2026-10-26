package com.example.admin.controller;

import com.example.admin.dto.*;
import com.example.admin.security.AdminPrincipal;
import com.example.admin.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final com.example.admin.rateLimit.AdminProfileRateLimiter adminProfileRateLimiter;

    /**
     * Register Sub Admin for one service. Main Admin Bearer token required.
     * Body: { "email", "password", "confirmPassword", "assignedService", ... }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AdminRegisterRequest request) {
        AdminRegisterResponse response = adminAuthService.registerAdmin(request);
        if (!response.isSuccess()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, response.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin password login. Called by User Service's unified /api/v1/auth/login
     * fallback when the email isn't found in user-service's own users table.
     */
    /**
     * Admin password login. Called by User Service's unified /api/v1/auth/login
     * fallback when the email isn't found in user-service's own users table.
     */
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(
                @RequestBody AdminLoginRequest request,
                HttpServletRequest httpRequest) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        return ResponseEntity.ok(adminAuthService.login(request, httpRequest));
    }

    /**
     * Admin login OTP request. Called by User Service's /api/v1/auth/login/otp/request fallback.
     */
    @PostMapping("/login/otp/request")
    public ResponseEntity<LoginOtpResponse> requestLoginOtp(@RequestBody LoginOtpRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email is required");
        }
        LoginOtpResponse response = adminAuthService.requestLoginOtp(request.getEmail());
        if (!response.isSuccess()) {
            HttpStatus status = response.getMessage() != null && response.getMessage().contains("does not exist")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            throw new org.springframework.web.server.ResponseStatusException(status, response.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Admin login OTP verify. Called by User Service's /api/v1/auth/login/otp/verify fallback.
     */
    @PostMapping("/login/otp/verify")
    public ResponseEntity<AdminLoginResponse> verifyLoginOtp(
                @RequestBody LoginOtpVerifyRequest request,
                HttpServletRequest httpRequest) {
        if (request == null || ((request.getEmail() == null || request.getEmail().isBlank()) && (request.getOtpSessionId() == null || request.getOtpSessionId().isBlank()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email or otpSessionId is required");
        }
        return ResponseEntity.ok(adminAuthService.verifyLoginOtp(request, httpRequest));
    }

    /**
     * Admin login OTP resend. Resends a new OTP for login using existing session.
     */
    @PostMapping("/login/otp/resend")
    public ResponseEntity<LoginOtpResponse> resendLoginOtp(@RequestBody ResendOtpRequest request) {
        if (request == null || request.getOtpSessionId() == null || request.getOtpSessionId().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "otpSessionId is required");
        }
        LoginOtpResponse response = adminAuthService.resendLoginOtp(request);
        if (!response.isSuccess()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, response.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Admin forgot password OTP request. Called by User Service's /api/v1/auth/password/forgot fallback.
     */
    @PostMapping("/password/forgot")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email is required");
        }
        ForgotPasswordResponse response = adminAuthService.forgotPassword(request.getEmail());
        if (!response.isSuccess()) {
            HttpStatus status = response.getMessage() != null && response.getMessage().contains("does not exist")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            throw new org.springframework.web.server.ResponseStatusException(status, response.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Admin verify password OTP. Called by User Service's /api/v1/auth/password/verify-otp fallback.
     */
    @PostMapping("/password/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyPasswordOtp(@RequestBody VerifyOtpRequest request) {
        if (request == null || ((request.getEmail() == null || request.getEmail().isBlank()) && (request.getOtpSessionId() == null || request.getOtpSessionId().isBlank()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email or otpSessionId is required");
        }
        VerifyOtpResponse response = adminAuthService.verifyPasswordOtp(request);
        if (!response.isSuccess()) {
            HttpStatus status = response.getMessage() != null && response.getMessage().contains("does not exist")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            throw new org.springframework.web.server.ResponseStatusException(status, response.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Admin password reset OTP resend. Resends a new OTP for password reset using existing session.
     */
    @PostMapping("/password/otp/resend")
    public ResponseEntity<ForgotPasswordResponse> resendPasswordOtp(@RequestBody ResendOtpRequest request) {
        if (request == null || request.getOtpSessionId() == null || request.getOtpSessionId().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "otpSessionId is required");
        }
        ForgotPasswordResponse response = adminAuthService.resendPasswordOtp(request);
        if (!response.isSuccess()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, response.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Admin reset password.
     */
    @PostMapping("/password/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(@RequestBody PasswordResetRequest request) {
        if (request == null || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "New password is required");
        }
        PasswordResetResponse response = adminAuthService.resetPassword(request);
        if (!response.isSuccess()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, response.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Update authenticated admin's own profile (PUT and PATCH supported).
     */
    @RequestMapping(value = {"/profile", "/me"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<AdminProfileResponse> updateProfile(
                @jakarta.validation.Valid @RequestBody UpdateAdminProfileRequest request,
                @AuthenticationPrincipal AdminPrincipal adminPrincipal,
                HttpServletRequest httpRequest) {

        if (adminPrincipal == null || adminPrincipal.getAdminId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication token missing or invalid");
        }

        adminProfileRateLimiter.checkLimit(adminPrincipal.getAdminId());

        AdminProfileResponse response =
                adminAuthService.updateProfile(adminPrincipal.getAdminId(), request, httpRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * Get authenticated admin's own profile.
     */
    @GetMapping({"/profile", "/me"})
    public ResponseEntity<AdminProfileResponse> getProfile(
                @AuthenticationPrincipal AdminPrincipal adminPrincipal,
                HttpServletRequest httpRequest) {

        if (adminPrincipal == null || adminPrincipal.getAdminId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication token missing or invalid");
        }

        return ResponseEntity.ok(adminAuthService.getProfile(adminPrincipal.getAdminId(), httpRequest));
    }
}
