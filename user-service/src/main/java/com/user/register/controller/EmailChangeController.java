package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.EmailChangeRequestDto;
import com.user.register.dto.EmailChangeResponse;
import com.user.register.dto.VerifyNewEmailDto;
import com.user.register.dto.VerifyOldEmailDto;
import com.user.register.service.EmailChangeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST controller exposing the email-change endpoints.
 *
 * <p>All endpoints require a valid JWT (Bearer token) in the Authorization header.
 * The security layer ({@code UnifiedSecurityConfig}) enforces authentication via
 * {@code /api/v1/users/email/**} being in the {@code .authenticated()} bucket.
 *
 * <p>Flow:
 * <ol>
 *   <li>POST /change-request  — re-auth + send OTPs to old + new email</li>
 *   <li>POST /verify-old      — submit OTP from old email</li>
 *   <li>POST /verify-new      — submit OTP from new email → email updated, sessions revoked</li>
 *   <li>GET /change-request/{sessionId} — retrieve email change session details (accessible by owner or admin)</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/v1/users/email")
@RequiredArgsConstructor
@Slf4j
public class EmailChangeController {

    private final EmailChangeService emailChangeService;

    // =========================================================================
    // STEP 1 — Initiate email change
    // =========================================================================

    /**
     * Initiates the email change flow.
     *
     * <p>Re-authenticates the user, validates the new email for uniqueness, creates
     * an {@code EmailChangeRequest}, and sends OTPs to both the old and new email addresses.
     *
     * <p><b>Request body:</b>
     * <pre>
     * {
     *   "newEmail": "user@newdomain.com",
     *   "password": "currentPassword123"   // option A
     *   // OR
     *   "reAuthOtpSessionId": "uuid"        // option B (verified OTP session)
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
    public ResponseEntity<ApiResponse<EmailChangeResponse>> initiateEmailChange(
            HttpServletRequest httpRequest,
            @Valid @RequestBody EmailChangeRequestDto dto) {

        log.info("[EmailChangeController] POST /change-request — initiating email change");

        EmailChangeResponse response = emailChangeService.initiateEmailChange(httpRequest, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "OTP codes sent to both your current and new email addresses. "
              + "Please verify both within 5 minutes.",
                response,
                LocalDateTime.now()));
    }

    // =========================================================================
    // STEP 2 — Verify old-email OTP
    // =========================================================================

    /**
     * Verifies the OTP that was sent to the user's current (old) email.
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
    public ResponseEntity<ApiResponse<EmailChangeResponse>> verifyOldEmail(
            HttpServletRequest httpRequest,
            @Valid @RequestBody VerifyOldEmailDto dto) {

        log.info("[EmailChangeController] POST /verify-old — verifying old-email OTP, sessionId={}",
                dto.getSessionId());

        EmailChangeResponse response = emailChangeService.verifyOldEmail(httpRequest, dto);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Current email OTP verified. Please now submit the OTP sent to your new email.",
                response,
                LocalDateTime.now()));
    }

    // =========================================================================
    // STEP 3 — Verify new-email OTP (final step)
    // =========================================================================

    /**
     * Verifies the OTP sent to the new email address and completes the email change.
     *
     * <p>On success:
     * <ul>
     *   <li>User's email is updated in the database.</li>
     *   <li>All existing JWT sessions are invalidated (access + refresh tokens blacklisted).</li>
     *   <li>The client must redirect the user to the login page.</li>
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
    public ResponseEntity<ApiResponse<EmailChangeResponse>> verifyNewEmail(
            HttpServletRequest httpRequest,
            @Valid @RequestBody VerifyNewEmailDto dto) {

        log.info("[EmailChangeController] POST /verify-new — verifying new-email OTP, sessionId={}",
                dto.getSessionId());

        EmailChangeResponse response = emailChangeService.verifyNewEmail(httpRequest, dto);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                true,
                "Email address successfully changed. All sessions have been invalidated. "
              + "Please log in again with your new email address.",
                response,
                LocalDateTime.now()));
    }

    // =========================================================================
    // STATUS QUERY — Admin / Owner Status Lookup
    // =========================================================================

    /**
     * Retrieves the status of an active or completed email change request.
     * Accessible by the owner of the request or any administrator (MAIN_ADMIN, SUB_ADMIN).
     *
     * <p><b>URL parameter:</b> {@code sessionId} (UUID)
     *
     * @param httpRequest the HTTP request (for extracting caller ID and role)
     * @param sessionId   the email change session ID
     * @return 200 with session details; or 401/403/404 on failure
     */
    @GetMapping("/change-request/{sessionId}")
    public ResponseEntity<ApiResponse<EmailChangeResponse>> getEmailChangeSession(
            HttpServletRequest httpRequest,
            @PathVariable String sessionId) {

        log.info("[EmailChangeController] GET /change-request/{} — retrieving email change session status", sessionId);

        EmailChangeResponse response = emailChangeService.getEmailChangeSession(httpRequest, sessionId);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Email change session retrieved successfully.",
                response,
                LocalDateTime.now()));
    }
}
