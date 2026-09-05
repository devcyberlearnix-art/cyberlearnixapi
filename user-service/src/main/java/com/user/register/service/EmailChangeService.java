package com.user.register.service;

import com.user.register.dto.EmailChangeRequestDto;
import com.user.register.dto.EmailChangeResponse;
import com.user.register.dto.VerifyNewEmailDto;
import com.user.register.dto.VerifyOldEmailDto;
import com.user.register.entity.EmailChangeAuditLog;
import com.user.register.entity.EmailChangeRequest;
import com.user.register.entity.User;
import com.user.register.entity.UserSession;
import com.user.register.exception.EmailChangeException;
import com.user.register.repository.EmailChangeAuditLogRepository;
import com.user.register.repository.EmailChangeRequestRepository;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.UnifiedJwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the three-step high-security email change flow:
 *
 * <ol>
 *   <li>Initiate — re-auth → create EmailChangeRequest → send OTPs to old + new email</li>
 *   <li>Verify Old — submit OTP received on old email</li>
 *   <li>Verify New — submit OTP received on new email → update email → invalidate JWTs</li>
 * </ol>
 *
 * <p>OTP state is managed entirely by the existing {@link OtpService} (Redis + SHA-256).
 * No OTP values are stored in the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailChangeService {

    // ===== OTP CONFIGURATION =====
    private static final String OTP_TYPE_OLD    = "EMAIL_CHANGE_OLD";
    private static final String OTP_TYPE_NEW    = "EMAIL_CHANGE_NEW";
    private static final String OTP_TYPE_REAUTH = "EMAIL_CHANGE_REAUTH";

    /** OTP is valid for 5 minutes. */
    private static final int OTP_VALID_MINUTES  = 5;
    /** Max verification attempts per OTP session. */
    private static final int OTP_MAX_ATTEMPTS   = 3;
    /** Cooldown between OTP sends (seconds). */
    private static final int OTP_COOLDOWN_SECS  = 60;
    /** Max OTP sends per email per hour. */
    private static final int OTP_MAX_PER_HOUR   = 3;
    /** Window for the hourly rate limit (seconds). */
    private static final int OTP_RATE_WINDOW    = 3600;

    /** EmailChangeRequest expires after this many minutes (OTP TTL + buffer). */
    private static final int REQUEST_EXPIRY_MINUTES = 10;

    // ===== DEPENDENCIES =====
    private final UserRepository                userRepository;
    private final UserSessionRepository         userSessionRepository;
    private final EmailChangeRequestRepository  emailChangeRequestRepository;
    private final EmailChangeAuditLogRepository auditLogRepository;
    private final OtpService                    otpService;
    private final EmailService                  emailService;
    private final TokenBlacklistService         tokenBlacklistService;
    private final UnifiedJwtService             jwtService;
    private final BCryptPasswordEncoder         passwordEncoder;

    // =========================================================================
    // STEP 1 — Initiate Email Change
    // =========================================================================

    /**
     * Validates the authenticated user, re-authenticates them, validates the
     * requested new email, creates a {@link EmailChangeRequest}, and dispatches
     * OTPs to both the old and new email addresses.
     *
     * @param httpRequest the incoming HTTP request (used for IP / user-agent extraction)
     * @param dto         the request payload
     * @return a populated {@link EmailChangeResponse} with the requestId and expiry time
     */
    @Transactional
    public EmailChangeResponse initiateEmailChange(HttpServletRequest httpRequest,
                                                   EmailChangeRequestDto dto) {

        User user = extractAuthenticatedUser(httpRequest);
        String currentEmail = user.getEmail();
        String newEmail     = dto.getNewEmail().trim().toLowerCase();

        // ── Guard: new email must differ from current ──────────────────────────
        if (currentEmail.equalsIgnoreCase(newEmail)) {
            throw EmailChangeException.badRequest(
                    "New email must be different from your current email.");
        }

        // ── Guard: new email must be globally unique ───────────────────────────
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw EmailChangeException.conflict(
                    "This email address is already registered. Please choose a different email.");
        }

        // ── Guard: no pending request for the same new email ──────────────────
        if (emailChangeRequestRepository.existsByNewEmailAndStatus(
                newEmail, EmailChangeRequest.Status.PENDING)) {
            throw EmailChangeException.conflict(
                    "This email address is already awaiting verification in another request.");
        }

        // ── Re-authentication ──────────────────────────────────────────────────
        performReAuthentication(user, dto);

        // ── Rate limit OTP sends ───────────────────────────────────────────────
        checkOtpSendRateLimit(currentEmail, OTP_TYPE_OLD);
        checkOtpSendRateLimit(newEmail,     OTP_TYPE_NEW);

        // ── Expire any existing PENDING request for this user ─────────────────
        int expired = emailChangeRequestRepository.expirePendingRequestsForUser(user.getId());
        if (expired > 0) {
            log.info("[EmailChange] Expired {} stale PENDING request(s) for userId={}", expired, user.getId());
        }

        // ── Generate OTPs and create Redis sessions ────────────────────────────
        String oldOtp = otpService.generateOtp();
        String newOtp = otpService.generateOtp();

        OtpService.OtpSession oldSession = otpService.createSession(
                currentEmail, OTP_TYPE_OLD, oldOtp, OTP_VALID_MINUTES, OTP_MAX_ATTEMPTS);

        OtpService.OtpSession newSession = otpService.createSession(
                newEmail, OTP_TYPE_NEW, newOtp, OTP_VALID_MINUTES, OTP_MAX_ATTEMPTS);

        // ── Persist EmailChangeRequest ─────────────────────────────────────────
        EmailChangeRequest request = EmailChangeRequest.builder()
                .userId(user.getId())
                .newEmail(newEmail)
                .oldOtpSessionId(oldSession.sessionId())
                .newOtpSessionId(newSession.sessionId())
                .expiryTime(LocalDateTime.now().plusMinutes(REQUEST_EXPIRY_MINUTES))
                .build();

        emailChangeRequestRepository.save(request);
        log.info("[EmailChange] Created session id={} for userId={}", request.getId(), user.getId());

        // ── Send OTP emails ────────────────────────────────────────────────────
        emailService.sendEmailChangeOtp(currentEmail, oldOtp);
        emailService.sendEmailChangeOtp(newEmail, newOtp);
        log.info("[EmailChange] OTP sent to old={} and new={}", currentEmail, maskEmail(newEmail));

        return EmailChangeResponse.builder()
                .sessionId(request.getId().toString())
                .oldEmailVerified(false)
                .newEmailVerified(false)
                .status(EmailChangeRequest.Status.PENDING.name())
                .expiresAt(request.getExpiryTime())
                .message("OTP codes sent to both your current and new email addresses. "
                       + "Please verify both within " + OTP_VALID_MINUTES + " minutes.")
                .build();
    }

    // =========================================================================
    // STEP 2 — Verify Old Email OTP
    // =========================================================================

    /**
     * Verifies the OTP that was sent to the user's current (old) email address.
     * Sets {@code oldEmailVerified = true} on the {@link EmailChangeRequest}.
     */
    @Transactional
    public EmailChangeResponse verifyOldEmail(HttpServletRequest httpRequest,
                                              VerifyOldEmailDto dto) {

        User user = extractAuthenticatedUser(httpRequest);
        EmailChangeRequest request = resolveAndValidateSession(dto.getSessionId(), user.getId());

        // ── Guard: already verified ────────────────────────────────────────────
        if (request.isOldEmailVerified()) {
            throw EmailChangeException.badRequest("Old email OTP has already been verified.");
        }

        // ── Verify OTP via Redis session ───────────────────────────────────────
        OtpService.OtpVerifyResult result = otpService.verifySession(
                request.getOldOtpSessionId(),
                user.getEmail(),
                dto.getOtp(),
                OTP_TYPE_OLD,
                false /* don't consume — keep session alive for audit */);

        handleOtpVerifyResult(result, "current email");

        // ── Mark old email as verified ─────────────────────────────────────────
        request.setOldEmailVerified(true);
        emailChangeRequestRepository.save(request);
        log.info("[EmailChange] Old email verified for sessionId={}", request.getId());

        return EmailChangeResponse.builder()
                .sessionId(request.getId().toString())
                .oldEmailVerified(true)
                .newEmailVerified(false)
                .status(request.getStatus().name())
                .expiresAt(request.getExpiryTime())
                .message("Current email verified. Please now enter the OTP sent to your new email address.")
                .build();
    }

    // =========================================================================
    // STEP 3 — Verify New Email OTP (Final Step)
    // =========================================================================

    /**
     * Verifies the OTP sent to the new email. If successful:
     * <ol>
     *   <li>Updates {@code user.email} in the database.</li>
     *   <li>Deletes all {@link UserSession} rows for the user.</li>
     *   <li>Blacklists all active JWTs (access + refresh) in Redis.</li>
     *   <li>Marks the {@link EmailChangeRequest} as VERIFIED.</li>
     *   <li>Writes an {@link EmailChangeAuditLog} record.</li>
     *   <li>Sends notification emails to both old and new addresses.</li>
     * </ol>
     */
    @Transactional
    public EmailChangeResponse verifyNewEmail(HttpServletRequest httpRequest,
                                              VerifyNewEmailDto dto) {

        User user    = extractAuthenticatedUser(httpRequest);
        String oldEmail = user.getEmail();
        EmailChangeRequest request = resolveAndValidateSession(dto.getSessionId(), user.getId());

        // ── Guard: old-email step must be completed first ──────────────────────
        if (!request.isOldEmailVerified()) {
            throw EmailChangeException.badRequest(
                    "You must verify your current email OTP before verifying the new one.");
        }

        // ── Guard: not already fully verified ─────────────────────────────────
        if (request.isNewEmailVerified()) {
            throw EmailChangeException.badRequest("New email OTP has already been verified.");
        }

        // ── Verify OTP via Redis session ───────────────────────────────────────
        OtpService.OtpVerifyResult result = otpService.verifySession(
                request.getNewOtpSessionId(),
                request.getNewEmail(),
                dto.getOtp(),
                OTP_TYPE_NEW,
                true /* consume — delete Redis session on success */);

        handleOtpVerifyResult(result, "new email");

        // ── Update user email ──────────────────────────────────────────────────
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        log.info("[EmailChange] Email updated userId={} oldEmail={} newEmail={}",
                user.getId(), maskEmail(oldEmail), maskEmail(request.getNewEmail()));

        // ── Invalidate all existing sessions / JWTs ────────────────────────────
        invalidateAllSessions(user);

        // ── Mark request as VERIFIED ───────────────────────────────────────────
        request.setNewEmailVerified(true);
        request.setStatus(EmailChangeRequest.Status.VERIFIED);
        emailChangeRequestRepository.save(request);

        // ── Audit log ─────────────────────────────────────────────────────────
        EmailChangeAuditLog auditLog = EmailChangeAuditLog.builder()
                .userId(user.getId())
                .oldEmail(oldEmail)
                .newEmail(request.getNewEmail())
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .status(EmailChangeAuditLog.STATUS_SUCCESS)
                .build();
        auditLogRepository.save(auditLog);

        // ── Notification emails ────────────────────────────────────────────────
        sendChangeNotificationEmails(oldEmail, request.getNewEmail(), user.getFirstName());

        return EmailChangeResponse.builder()
                .sessionId(request.getId().toString())
                .oldEmailVerified(true)
                .newEmailVerified(true)
                .status(EmailChangeRequest.Status.VERIFIED.name())
                .expiresAt(request.getExpiryTime())
                .message("Email address successfully changed. Please log in again with your new email.")
                .requiresReLogin(true)
                .build();
    }

    /**
     * Retrieves the status of an email change session by its sessionId.
     * Accessible by either the requesting user or an administrator (MAIN_ADMIN or SUB_ADMIN).
     */
    public EmailChangeResponse getEmailChangeSession(HttpServletRequest httpRequest, String sessionId) {
        User user = extractAuthenticatedUser(httpRequest);

        UUID sessionUuid;
        try {
            sessionUuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw EmailChangeException.badRequest("Invalid session ID format.");
        }

        EmailChangeRequest request = emailChangeRequestRepository.findById(sessionUuid)
                .orElseThrow(() -> EmailChangeException.notFound("Email change request session not found."));

        // Auth check: Must be owner OR an admin (MAIN_ADMIN or SUB_ADMIN)
        boolean isOwner = request.getUserId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.MAIN_ADMIN || user.getRole() == User.Role.SUB_ADMIN;

        if (!isOwner && !isAdmin) {
            throw EmailChangeException.unauthorized("Access denied. You do not have permission to view this email change session.");
        }

        return EmailChangeResponse.builder()
                .sessionId(request.getId().toString())
                .oldEmailVerified(request.isOldEmailVerified())
                .newEmailVerified(request.isNewEmailVerified())
                .status(request.getStatus().name())
                .expiresAt(request.getExpiryTime())
                .message("Email change session retrieved successfully.")
                .build();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Extracts the authenticated {@link User} from the JWT bearer token in the request.
     * Throws {@link EmailChangeException} with 401 if the token is missing, invalid, or
     * the user no longer exists.
     */
    private User extractAuthenticatedUser(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw EmailChangeException.unauthorized("Authorization token is required.");
        }

        String token = authHeader.substring(7);
        String userId;
        try {
            userId = jwtService.extractUserId(token);
        } catch (Exception e) {
            log.warn("[EmailChange] JWT extraction failed: {}", e.getMessage());
            throw EmailChangeException.unauthorized("Invalid or expired authorization token.");
        }

        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> EmailChangeException.notFound("Authenticated user not found."));
    }

    /**
     * Resolves and validates an {@link EmailChangeRequest} by its UUID string ID.
     * Performs userId ownership check, expiry check, and completed-state check.
     */
    private EmailChangeRequest resolveAndValidateSession(String sessionId, UUID userId) {
        UUID sessionUuid;
        try {
            sessionUuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw EmailChangeException.badRequest("Invalid session ID format.");
        }

        EmailChangeRequest request = emailChangeRequestRepository
                .findByIdAndUserId(sessionUuid, userId)
                .orElseThrow(() -> EmailChangeException.notFound(
                        "Email change request not found or does not belong to you."));

        if (request.getStatus() == EmailChangeRequest.Status.EXPIRED || request.isExpired()) {
            // Persist the EXPIRED status if not already set
            if (request.getStatus() != EmailChangeRequest.Status.EXPIRED) {
                request.setStatus(EmailChangeRequest.Status.EXPIRED);
                emailChangeRequestRepository.save(request);
            }
            throw EmailChangeException.gone(
                    "This email change request session has expired. Please start a new request.");
        }

        if (request.getStatus() == EmailChangeRequest.Status.VERIFIED) {
            throw EmailChangeException.badRequest(
                    "This email change request session has already been completed.");
        }

        return request;
    }

    /**
     * Performs re-authentication using either a password or a pre-verified OTP session.
     * Exactly one of {@code password} or {@code reAuthOtpSessionId} must be provided.
     */
    private void performReAuthentication(User user, EmailChangeRequestDto dto) {
        boolean hasPassword  = dto.getPassword() != null && !dto.getPassword().isBlank();
        boolean hasOtpSession = dto.getReAuthOtpSessionId() != null
                             && !dto.getReAuthOtpSessionId().isBlank();

        if (!hasPassword && !hasOtpSession) {
            throw EmailChangeException.badRequest(
                    "Re-authentication required. Provide either your password or a valid OTP session ID.");
        }
        if (hasPassword && hasOtpSession) {
            throw EmailChangeException.badRequest(
                    "Provide either your password or an OTP session ID, not both.");
        }

        if (hasPassword) {
            // Password-based re-auth
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                throw EmailChangeException.badRequest(
                        "Password-based re-authentication is not available for social-login accounts. "
                      + "Please use OTP-based re-authentication.");
            }
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw EmailChangeException.unauthorized("Incorrect password. Re-authentication failed.");
            }
            log.debug("[EmailChange] Password re-auth succeeded for userId={}", user.getId());

        } else {
            // OTP-session-based re-auth
            boolean isVerified = otpService.isSessionVerified(
                    dto.getReAuthOtpSessionId(), user.getEmail(), OTP_TYPE_REAUTH);

            if (!isVerified) {
                throw EmailChangeException.unauthorized(
                        "Re-authentication OTP session is invalid, expired, or not yet verified.");
            }
            // Consume the re-auth session so it cannot be reused
            otpService.deleteSession(dto.getReAuthOtpSessionId());
            log.debug("[EmailChange] OTP re-auth session consumed for userId={}", user.getId());
        }
    }

    /**
     * Checks the OTP send rate limit for the given email + OTP type.
     * Throws {@link EmailChangeException} with 429 if the limit is exceeded.
     */
    private void checkOtpSendRateLimit(String email, String otpType) {
        OtpService.OtpSendClaim claim = otpService.claimOtpSend(
                email, otpType, OTP_COOLDOWN_SECS, OTP_MAX_PER_HOUR, OTP_RATE_WINDOW);

        if (!claim.allowed()) {
            if (claim.hourlyLimitReached()) {
                throw EmailChangeException.tooManyRequests(
                        "You have reached the maximum number of OTP requests. "
                      + "Please try again after " + claim.retryAfterSeconds() + " seconds.");
            }
            throw EmailChangeException.tooManyRequests(
                    "Please wait " + claim.retryAfterSeconds()
                  + " seconds before requesting another OTP.");
        }
    }

    /**
     * Interprets an {@link OtpService.OtpVerifyResult} and throws an appropriate
     * {@link EmailChangeException} on failure.
     *
     * @param result      the result from the OTP verification
     * @param emailLabel  human-readable label ("current email" / "new email") for error messages
     */
    private void handleOtpVerifyResult(OtpService.OtpVerifyResult result, String emailLabel) {
        if (result.valid()) {
            return;
        }

        String reason = result.reason();
        int remaining = result.remainingAttempts();

        if (reason.contains("expired") || reason.contains("not found")) {
            throw EmailChangeException.gone(
                    "The OTP for your " + emailLabel + " has expired. Please start a new email change request.");
        }
        if (reason.contains("exhausted") || remaining <= 0) {
            throw EmailChangeException.tooManyRequests(
                    "Maximum OTP attempts exceeded for your " + emailLabel
                  + ". Please start a new email change request.");
        }
        if (reason.contains("already used")) {
            throw EmailChangeException.badRequest("OTP has already been used.");
        }

        throw EmailChangeException.badRequest(
                "Invalid OTP for your " + emailLabel + ". "
              + remaining + " attempt(s) remaining.");
    }

    /**
     * Deletes all {@link UserSession} rows for the user and blacklists their
     * access and refresh tokens in Redis, forcing a re-login.
     */
    private void invalidateAllSessions(User user) {
        List<UserSession> sessions = userSessionRepository.findByUser(user);
        log.info("[EmailChange] Invalidating {} session(s) for userId={}", sessions.size(), user.getId());

        for (UserSession session : sessions) {
            if (session.getAccessToken() != null && !session.getAccessToken().isBlank()) {
                tokenBlacklistService.blacklistToken(session.getAccessToken());
            }
            if (session.getRefreshToken() != null && !session.getRefreshToken().isBlank()) {
                tokenBlacklistService.blacklistToken(session.getRefreshToken());
            }
        }
        userSessionRepository.deleteByUser(user);
        log.info("[EmailChange] All sessions deleted for userId={}", user.getId());
    }

    /**
     * Sends post-change notification emails to both the old and new email addresses.
     * Errors here are logged but do not roll back the transaction.
     */
    private void sendChangeNotificationEmails(String oldEmail, String newEmail, String firstName) {
        String displayName = firstName != null ? firstName : "User";
        try {
            emailService.sendEmailChangeNotification(
                    oldEmail,
                    "Your CyberLearnix email address has been changed",
                    "Hi " + displayName + ",\n\n"
                  + "This is a security notification to inform you that the email address "
                  + "associated with your CyberLearnix account has been successfully changed "
                  + "to: " + newEmail + "\n\n"
                  + "If you did not make this change, please contact support immediately.\n\n"
                  + "— The CyberLearnix Team");
        } catch (Exception e) {
            log.error("[EmailChange] Failed to send notification to old email {}: {}", maskEmail(oldEmail), e.getMessage());
        }

        try {
            emailService.sendEmailChangeNotification(
                    newEmail,
                    "Your new email address is verified — CyberLearnix",
                    "Hi " + displayName + ",\n\n"
                  + "Your new email address has been successfully verified and is now the primary "
                  + "login email for your CyberLearnix account.\n\n"
                  + "Please use this email address when logging in from now on.\n\n"
                  + "— The CyberLearnix Team");
        } catch (Exception e) {
            log.error("[EmailChange] Failed to send confirmation to new email {}: {}", maskEmail(newEmail), e.getMessage());
        }
    }

    /**
     * Extracts the real client IP, respecting common reverse-proxy headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Masks an email address for safe logging (e.g. {@code us***@example.com}).
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String visible = local.length() > 2 ? local.substring(0, 2) : local.substring(0, 1);
        return visible + "***@" + parts[1];
    }
}
