package com.example.admin.controller;

import com.example.admin.dto.AdminEmailChangeRequestDto;
import com.example.admin.dto.AdminEmailChangeResponse;
import com.example.admin.dto.AdminVerifyNewEmailDto;
import com.example.admin.dto.AdminVerifyOldEmailDto;
import com.example.admin.dto.ApiResponse;
import com.example.admin.service.AdminEmailChangeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST controller exposing the admin email-change endpoints.
 *
 * <p>All endpoints require a valid JWT (Bearer token) in the Authorization header.
 * The security layer ({@code SecurityConfig}) enforces authentication via
 * {@code /api/v1/admin/**} being in the {@code .authenticated()} bucket.
 *
 * <p>Flow:
 * <ol>
 *   <li>POST /api/v1/admin/email/change-request  — re-auth + send OTPs to old + new email</li>
 *   <li>POST /api/v1/admin/email/verify-old      — submit OTP from old email</li>
 *   <li>POST /api/v1/admin/email/verify-new      — submit OTP from new email → email updated, token revoked</li>
 *   <li>GET  /api/v1/admin/email/change-request/{sessionId} — retrieve session status (owner or MAIN_ADMIN)</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/v1/admin/email")
@RequiredArgsConstructor
@Slf4j
public class AdminEmailChangeController {

    private final AdminEmailChangeService adminEmailChangeService;

    // =========================================================================
    // STEP 1 — Initiate email change
    // =========================================================================

    /**
     * Initiates the admin email change flow.
     *
     * <p>Re-authenticates the admin, validates the new email for uniqueness, creates
     * an {@code AdminEmailChangeRequest}, and sends OTPs to both the old and new email addresses.
     *
     * <p><b>Request body:</b>
     * <pre>
     * {
     *   "newEmail": "admin@newdomain.com",
     *   "password": "currentPassword123"   // option A
     *   // OR
     *   "reAuthOtpSessionId": "uuid"        // option B (pre-verified OTP session)
     * }
     * </pre>
     *
     * <p><b>Response 200:</b>
     * <pre>
     * {
     *   "success": true,
     *   "message": "OTP codes sent to both your current and new email addresses.",
     *   "data": {
     *     "sessionId": "uuid",
     *     "oldEmailVerified": false,
     *     "newEmailVerified": false,
     *     "status": "PENDING",
     *     "expiresAt": "2026-08-18T19:30:00"
     *   }
     * }
     * </pre>
     *
     * @param httpRequest the HTTP request (used for IP / user-agent)
     * @param dto         the email change initiation payload
     * @return 200 with the sessionId and status; or 4xx on validation/auth failure
     */
    @PostMapping("/change-request")
    public ResponseEntity<ApiResponse<AdminEmailChangeResponse>> initiateEmailChange(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AdminEmailChangeRequestDto dto) {

        log.info("[AdminEmailChangeController] POST /change-request — initiating admin email change");

        AdminEmailChangeResponse response = adminEmailChangeService.initiateEmailChange(httpRequest, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "OTP codes sent to both your current and new email addresses. "
              + "Please verify both within 5 minutes.",
                response,
                LocalDateTime.now().toString()));
    }

    // =========================================================================
    // STEP 2 — Verify old-email OTP
    // =========================================================================

    /**
     * Verifies the OTP that was sent to the admin's current (old) email.
     *
     * <p><b>Request body:</b>
     * <pre>
     * {
     *   "sessionId": "uuid-from-step-1",
     *   "otp":       "123456"
     * }
     * </pre>
     *
     * <p><b>Response 200:</b> {@code oldEmailVerified: true}
     *
     * @param httpRequest the HTTP request (for auth token extraction)
     * @param dto         the verification payload
     * @return 200 with updated verification flags; or 4xx on failure
     */
    @PostMapping("/verify-old")
    public ResponseEntity<ApiResponse<AdminEmailChangeResponse>> verifyOldEmail(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AdminVerifyOldEmailDto dto) {

        log.info("[AdminEmailChangeController] POST /verify-old — verifying old-email OTP, sessionId={}",
                dto.getSessionId());

        AdminEmailChangeResponse response = adminEmailChangeService.verifyOldEmail(httpRequest, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Current email OTP verified. Please now submit the OTP sent to your new email.",
                response,
                LocalDateTime.now().toString()));
    }

    // =========================================================================
    // STEP 3 — Verify new-email OTP (final step)
    // =========================================================================

    /**
     * Verifies the OTP sent to the new email address and completes the email change.
     *
     * <p>On success:
     * <ul>
     *   <li>Admin's email is updated in the database.</li>
     *   <li>The current JWT access token is blacklisted in Redis (forces re-login).</li>
     *   <li>The client must redirect the admin to the login page.</li>
     * </ul>
     *
     * <p><b>Request body:</b>
     * <pre>
     * {
     *   "sessionId": "uuid-from-step-1",
     *   "otp":       "654321"
     * }
     * </pre>
     *
     * <p><b>Response 200:</b> {@code requiresReLogin: true} — client must redirect to /login
     *
     * @param httpRequest the HTTP request
     * @param dto         the verification payload
     * @return 200 on success; or 4xx on OTP failure / state violation
     */
    @PostMapping("/verify-new")
    public ResponseEntity<ApiResponse<AdminEmailChangeResponse>> verifyNewEmail(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AdminVerifyNewEmailDto dto) {

        log.info("[AdminEmailChangeController] POST /verify-new — verifying new-email OTP, sessionId={}",
                dto.getSessionId());

        AdminEmailChangeResponse response = adminEmailChangeService.verifyNewEmail(httpRequest, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Email address successfully changed. Your session has been revoked. "
              + "Please log in again with your new email address.",
                response,
                LocalDateTime.now().toString()));
    }

    // =========================================================================
    // STATUS QUERY — Owner / MAIN_ADMIN Status Lookup
    // =========================================================================

    /**
     * Retrieves the status of an active or completed admin email change session.
     * Accessible by the owner of the request or any MAIN_ADMIN.
     *
     * <p><b>URL parameter:</b> {@code sessionId} (UUID)
     *
     * @param httpRequest the HTTP request (for extracting caller ID and role)
     * @param sessionId   the email change session ID
     * @return 200 with session details; or 401/403/404 on failure
     */
    @GetMapping("/change-request/{sessionId}")
    public ResponseEntity<ApiResponse<AdminEmailChangeResponse>> getEmailChangeSession(
            HttpServletRequest httpRequest,
            @PathVariable String sessionId) {

        log.info("[AdminEmailChangeController] GET /change-request/{} — retrieving email change session status",
                sessionId);

        AdminEmailChangeResponse response = adminEmailChangeService.getEmailChangeSession(httpRequest, sessionId);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Email change session retrieved successfully.",
                response,
                LocalDateTime.now().toString()));
    }
}
