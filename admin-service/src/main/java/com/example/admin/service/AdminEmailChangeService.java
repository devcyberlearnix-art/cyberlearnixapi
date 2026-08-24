package com.example.admin.service;

import com.example.admin.dto.AdminEmailChangeRequestDto;
import com.example.admin.dto.AdminEmailChangeResponse;
import com.example.admin.dto.AdminVerifyNewEmailDto;
import com.example.admin.dto.AdminVerifyOldEmailDto;
import com.example.admin.entity.Admin;
import com.example.admin.entity.AdminEmailChangeAuditLog;
import com.example.admin.entity.AdminEmailChangeRequest;
import com.example.admin.exception.AdminEmailChangeException;
import com.example.admin.repository.AdminEmailChangeAuditLogRepository;
import com.example.admin.repository.AdminEmailChangeRequestRepository;
import com.example.admin.repository.AdminRepository;
import com.example.admin.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orchestrates the three-step high-security email change flow for Admin accounts:
 *
 * <ol>
 *   <li>Initiate — re-auth → create AdminEmailChangeRequest → send OTPs to old + new email</li>
 *   <li>Verify Old — submit OTP received on old (current) email</li>
 *   <li>Verify New — submit OTP received on new email → update email → blacklist current JWT</li>
 * </ol>
 *
 * <p>OTP state is managed entirely by {@link OtpService} (Redis + SHA-256).
 * No OTP values are stored in the database.
 *
 * <p>Since admin-service is stateless (no AdminSession table), JWT invalidation
 * is achieved by blacklisting the current access token in Redis until its natural expiry.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmailChangeService {

    // ===== OTP CONFIGURATION =====
    private static final String OTP_TYPE_OLD    = "ADMIN_EMAIL_CHANGE_OLD";
    private static final String OTP_TYPE_NEW    = "ADMIN_EMAIL_CHANGE_NEW";
    private static final String OTP_TYPE_REAUTH = "ADMIN_EMAIL_CHANGE_REAUTH";

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

    /** AdminEmailChangeRequest expires after this many minutes (OTP TTL + buffer). */
    private static final int REQUEST_EXPIRY_MINUTES = 10;

    /** Redis key prefix for blacklisted JWT tokens. */
    private static final String TOKEN_BLACKLIST_PREFIX = "ADMIN:JWT:BLACKLIST:";

    // ===== DEPENDENCIES =====
    private final AdminRepository                     adminRepository;
    private final AdminEmailChangeRequestRepository   emailChangeRequestRepository;
    private final AdminEmailChangeAuditLogRepository  auditLogRepository;
    private final OtpService                          otpService;
    private final EmailService                        emailService;
    private final JwtService                          jwtService;
    private final PasswordEncoder                     passwordEncoder;
    private final StringRedisTemplate                 redisTemplate;

    // =========================================================================
    // STEP 1 — Initiate Email Change
    // =========================================================================

    /**
     * Validates the authenticated admin, re-authenticates them, validates the
     * requested new email, creates a {@link AdminEmailChangeRequest}, and dispatches
     * OTPs to both the old and new email addresses.
     *
     * @param httpRequest the incoming HTTP request (used for IP / user-agent extraction)
     * @param dto         the request payload
     * @return a populated {@link AdminEmailChangeResponse} with the sessionId and expiry time
     */
    @Transactional
    public AdminEmailChangeResponse initiateEmailChange(HttpServletRequest httpRequest,
                                                        AdminEmailChangeRequestDto dto) {

        Admin admin = extractAuthenticatedAdmin(httpRequest);
        String currentEmail = admin.getEmail();
        String newEmail     = dto.getNewEmail().trim().toLowerCase();

        // ── Guard: new email must differ from current ──────────────────────────
        if (currentEmail.equalsIgnoreCase(newEmail)) {
            throw AdminEmailChangeException.badRequest(
                    "New email must be different from your current email.");
        }

        // ── Guard: new email must be globally unique ───────────────────────────
        if (adminRepository.findByEmail(newEmail).isPresent()) {
            throw AdminEmailChangeException.conflict(
                    "This email address is already registered. Please choose a different email.");
        }

        // ── Guard: no pending request for the same new email ──────────────────
        if (emailChangeRequestRepository.existsByNewEmailAndStatus(
                newEmail, AdminEmailChangeRequest.Status.PENDING)) {
            throw AdminEmailChangeException.conflict(
                    "This email address is already awaiting verification in another request.");
        }

        // ── Re-authentication ──────────────────────────────────────────────────
        performReAuthentication(admin, dto);

        // ── Rate limit OTP sends ───────────────────────────────────────────────
        checkOtpSendRateLimit(currentEmail, OTP_TYPE_OLD);
        checkOtpSendRateLimit(newEmail,     OTP_TYPE_NEW);

        // ── Expire any existing PENDING request for this admin ─────────────────
        int expired = emailChangeRequestRepository.expirePendingRequestsForAdmin(admin.getId());
        if (expired > 0) {
            log.info("[AdminEmailChange] Expired {} stale PENDING request(s) for adminId={}", expired, admin.getId());
        }

        // ── Generate OTPs and create Redis sessions ────────────────────────────
        String oldOtp = otpService.generateOtp();
        String newOtp = otpService.generateOtp();

        OtpService.OtpSession oldSession = otpService.createSession(
                currentEmail, OTP_TYPE_OLD, oldOtp, OTP_VALID_MINUTES, OTP_MAX_ATTEMPTS);

        OtpService.OtpSession newSession = otpService.createSession(
                newEmail, OTP_TYPE_NEW, newOtp, OTP_VALID_MINUTES, OTP_MAX_ATTEMPTS);

        // ── Persist AdminEmailChangeRequest ────────────────────────────────────
        AdminEmailChangeRequest request = AdminEmailChangeRequest.builder()
                .adminId(admin.getId())
                .newEmail(newEmail)
                .oldOtpSessionId(oldSession.sessionId())
                .newOtpSessionId(newSession.sessionId())
                .expiryTime(LocalDateTime.now().plusMinutes(REQUEST_EXPIRY_MINUTES))
                .build();

        emailChangeRequestRepository.save(request);
        log.info("[AdminEmailChange] Created session id={} for adminId={}", request.getId(), admin.getId());

        // ── Send OTP emails ────────────────────────────────────────────────────
        emailService.sendEmailChangeOtp(currentEmail, oldOtp);
        emailService.sendEmailChangeOtp(newEmail, newOtp);
        log.info("[AdminEmailChange] OTP sent to old={} and new={}", currentEmail, maskEmail(newEmail));

        return AdminEmailChangeResponse.builder()
                .sessionId(request.getId().toString())
                .oldEmailVerified(false)
                .newEmailVerified(false)
                .status(AdminEmailChangeRequest.Status.PENDING.name())
                .expiresAt(request.getExpiryTime())
                .message("OTP codes sent to both your current and new email addresses. "
                       + "Please verify both within " + OTP_VALID_MINUTES + " minutes.")
                .build();
    }

    // =========================================================================
    // STEP 2 — Verify Old Email OTP
    // =========================================================================

    /**
     * Verifies the OTP that was sent to the admin's current (old) email address.
     * Sets {@code oldEmailVerified = true} on the {@link AdminEmailChangeRequest}.
     */
    @Transactional
    public AdminEmailChangeResponse verifyOldEmail(HttpServletRequest httpRequest,
                                                   AdminVerifyOldEmailDto dto) {

        Admin admin = extractAuthenticatedAdmin(httpRequest);
        AdminEmailChangeRequest request = resolveAndValidateSession(dto.getSessionId(), admin.getId());

        // ── Guard: already verified ────────────────────────────────────────────
        if (request.isOldEmailVerified()) {
            throw AdminEmailChangeException.badRequest("Old email OTP has already been verified.");
        }

        // ── Verify OTP via Redis session ───────────────────────────────────────
        OtpService.OtpVerifyResult result = otpService.verifySession(
                request.getOldOtpSessionId(),
                admin.getEmail(),
                dto.getOtp(),
                OTP_TYPE_OLD,
                false /* don't consume — keep session alive for audit */);

        handleOtpVerifyResult(result, "current email");

        // ── Mark old email as verified ─────────────────────────────────────────
        request.setOldEmailVerified(true);
        emailChangeRequestRepository.save(request);
        log.info("[AdminEmailChange] Old email verified for sessionId={}", request.getId());

        return AdminEmailChangeResponse.builder()
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
     *   <li>Updates {@code admin.email} in the database.</li>
     *   <li>Blacklists the current access token in Redis (forces re-login).</li>
     *   <li>Marks the {@link AdminEmailChangeRequest} as VERIFIED.</li>
     *   <li>Writes an {@link AdminEmailChangeAuditLog} record.</li>
     *   <li>Sends notification emails to both old and new addresses.</li>
     * </ol>
     */
    @Transactional
    public AdminEmailChangeResponse verifyNewEmail(HttpServletRequest httpRequest,
                                                   AdminVerifyNewEmailDto dto) {

        Admin admin    = extractAuthenticatedAdmin(httpRequest);
        String oldEmail = admin.getEmail();
        AdminEmailChangeRequest request = resolveAndValidateSession(dto.getSessionId(), admin.getId());

        // ── Guard: old-email step must be completed first ──────────────────────
        if (!request.isOldEmailVerified()) {
            throw AdminEmailChangeException.badRequest(
                    "You must verify your current email OTP before verifying the new one.");
        }

        // ── Guard: not already fully verified ─────────────────────────────────
        if (request.isNewEmailVerified()) {
            throw AdminEmailChangeException.badRequest("New email OTP has already been verified.");
        }

        // ── Verify OTP via Redis session ───────────────────────────────────────
        OtpService.OtpVerifyResult result = otpService.verifySession(
                request.getNewOtpSessionId(),
                request.getNewEmail(),
                dto.getOtp(),
                OTP_TYPE_NEW,
                true /* consume — delete Redis session on success */);

        handleOtpVerifyResult(result, "new email");

        // ── Update admin email ─────────────────────────────────────────────────
        admin.setEmail(request.getNewEmail());
        adminRepository.save(admin);
        log.info("[AdminEmailChange] Email updated adminId={} oldEmail={} newEmail={}",
                admin.getId(), maskEmail(oldEmail), maskEmail(request.getNewEmail()));

        // ── Blacklist current JWT (force re-login) ─────────────────────────────
        blacklistCurrentToken(httpRequest);

        // ── Mark request as VERIFIED ───────────────────────────────────────────
        request.setNewEmailVerified(true);
        request.setStatus(AdminEmailChangeRequest.Status.VERIFIED);
        emailChangeRequestRepository.save(request);

        // ── Audit log ─────────────────────────────────────────────────────────
        AdminEmailChangeAuditLog auditLog = AdminEmailChangeAuditLog.builder()
                .adminId(admin.getId())
                .oldEmail(oldEmail)
                .newEmail(request.getNewEmail())
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .status(AdminEmailChangeAuditLog.STATUS_SUCCESS)
                .build();
        auditLogRepository.save(auditLog);

        // ── Notification emails ────────────────────────────────────────────────
        sendChangeNotificationEmails(oldEmail, request.getNewEmail(), admin.getFirstName());

        return AdminEmailChangeResponse.builder()
                .sessionId(request.getId().toString())
                .oldEmailVerified(true)
                .newEmailVerified(true)
                .status(AdminEmailChangeRequest.Status.VERIFIED.name())
                .expiresAt(request.getExpiryTime())
                .message("Email address successfully changed. Please log in again with your new email.")
                .requiresReLogin(true)
                .build();
    }

    /**
     * Retrieves the status of an admin email change session by its sessionId.
     * Only the owning admin or a MAIN_ADMIN may access this endpoint.
     */
    public AdminEmailChangeResponse getEmailChangeSession(HttpServletRequest httpRequest, String sessionId) {
        Admin admin = extractAuthenticatedAdmin(httpRequest);

        UUID sessionUuid;
        try {
            sessionUuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw AdminEmailChangeException.badRequest("Invalid session ID format.");
        }

        AdminEmailChangeRequest request = emailChangeRequestRepository.findById(sessionUuid)
                .orElseThrow(() -> AdminEmailChangeException.notFound("Email change request session not found."));

        // Auth check: must be the owner OR a MAIN_ADMIN
        boolean isOwner   = request.getAdminId().equals(admin.getId());
        boolean isMainAdmin = "MAIN_ADMIN".equalsIgnoreCase(admin.getRole())
                           || "ROLE_MAIN_ADMIN".equalsIgnoreCase(admin.getRole());

        if (!isOwner && !isMainAdmin) {
            throw AdminEmailChangeException.unauthorized(
                    "Access denied. You do not have permission to view this email change session.");
        }

        return AdminEmailChangeResponse.builder()
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
     * Extracts the authenticated {@link Admin} from the JWT bearer token in the request.
     */
    private Admin extractAuthenticatedAdmin(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw AdminEmailChangeException.unauthorized("Authorization token is required.");
        }

        String token = authHeader.substring(7);
        UUID adminId;
        try {
            adminId = jwtService.extractAdminId(token);
        } catch (Exception e) {
            log.warn("[AdminEmailChange] JWT extraction failed: {}", e.getMessage());
            throw AdminEmailChangeException.unauthorized("Invalid or expired authorization token.");
        }

        return adminRepository.findById(adminId)
                .orElseThrow(() -> AdminEmailChangeException.notFound("Authenticated admin not found."));
    }

    /**
     * Resolves and validates an {@link AdminEmailChangeRequest} by its UUID string ID.
     * Performs adminId ownership check, expiry check, and completed-state check.
     */
    private AdminEmailChangeRequest resolveAndValidateSession(String sessionId, UUID adminId) {
        UUID sessionUuid;
        try {
            sessionUuid = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw AdminEmailChangeException.badRequest("Invalid session ID format.");
        }

        AdminEmailChangeRequest request = emailChangeRequestRepository
                .findByIdAndAdminId(sessionUuid, adminId)
                .orElseThrow(() -> AdminEmailChangeException.notFound(
                        "Email change request not found or does not belong to you."));

        if (request.getStatus() == AdminEmailChangeRequest.Status.EXPIRED || request.isExpired()) {
            if (request.getStatus() != AdminEmailChangeRequest.Status.EXPIRED) {
                request.setStatus(AdminEmailChangeRequest.Status.EXPIRED);
                emailChangeRequestRepository.save(request);
            }
            throw AdminEmailChangeException.gone(
                    "This email change request session has expired. Please start a new request.");
        }

        if (request.getStatus() == AdminEmailChangeRequest.Status.VERIFIED) {
            throw AdminEmailChangeException.badRequest(
                    "This email change request session has already been completed.");
        }

        return request;
    }

    /**
     * Performs re-authentication using either a password or a pre-verified OTP session.
     * Exactly one of {@code password} or {@code reAuthOtpSessionId} must be provided.
     */
    private void performReAuthentication(Admin admin, AdminEmailChangeRequestDto dto) {
        boolean hasPassword   = dto.getPassword() != null && !dto.getPassword().isBlank();
        boolean hasOtpSession = dto.getReAuthOtpSessionId() != null
                             && !dto.getReAuthOtpSessionId().isBlank();

        if (!hasPassword && !hasOtpSession) {
            throw AdminEmailChangeException.badRequest(
                    "Re-authentication required. Provide either your password or a valid OTP session ID.");
        }
        if (hasPassword && hasOtpSession) {
            throw AdminEmailChangeException.badRequest(
                    "Provide either your password or an OTP session ID, not both.");
        }

        if (hasPassword) {
            if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
                throw AdminEmailChangeException.unauthorized("Incorrect password. Re-authentication failed.");
            }
            log.debug("[AdminEmailChange] Password re-auth succeeded for adminId={}", admin.getId());
        } else {
            boolean isVerified = otpService.isSessionVerified(
                    dto.getReAuthOtpSessionId(), admin.getEmail(), OTP_TYPE_REAUTH);

            if (!isVerified) {
                throw AdminEmailChangeException.unauthorized(
                        "Re-authentication OTP session is invalid, expired, or not yet verified.");
            }
            otpService.deleteSession(dto.getReAuthOtpSessionId());
            log.debug("[AdminEmailChange] OTP re-auth session consumed for adminId={}", admin.getId());
        }
    }

    /**
     * Checks the OTP send rate limit for the given email + OTP type.
     * Uses Redis cooldown and hourly count keys managed by OtpService.
     */
    private void checkOtpSendRateLimit(String email, String otpType) {
        long cooldown = otpService.getCooldownSeconds(email, otpType);
        if (cooldown > 0) {
            throw AdminEmailChangeException.tooManyRequests(
                    "Please wait " + cooldown + " seconds before requesting another OTP.");
        }
        otpService.markCooldown(email, otpType, OTP_COOLDOWN_SECS);
    }

    /**
     * Interprets an {@link OtpService.OtpVerifyResult} and throws an appropriate
     * {@link AdminEmailChangeException} on failure.
     */
    private void handleOtpVerifyResult(OtpService.OtpVerifyResult result, String emailLabel) {
        if (result.valid()) {
            return;
        }

        String reason    = result.reason();
        int    remaining = result.remainingAttempts();

        if (reason.contains("expired") || reason.contains("not found")) {
            throw AdminEmailChangeException.gone(
                    "The OTP for your " + emailLabel + " has expired. Please start a new email change request.");
        }
        if (reason.contains("exhausted") || remaining <= 0) {
            throw AdminEmailChangeException.tooManyRequests(
                    "Maximum OTP attempts exceeded for your " + emailLabel
                  + ". Please start a new email change request.");
        }
        if (reason.contains("already used")) {
            throw AdminEmailChangeException.badRequest("OTP has already been used.");
        }

        throw AdminEmailChangeException.badRequest(
                "Invalid OTP for your " + emailLabel + ". "
              + remaining + " attempt(s) remaining.");
    }

    /**
     * Blacklists the current JWT access token in Redis until it naturally expires.
     * This forces the admin to log in again after email change completion.
     */
    private void blacklistCurrentToken(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            return;
        }
        try {
            // Store in Redis for the remaining JWT lifetime (15 min access token)
            String key = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(15));
            log.info("[AdminEmailChange] Current access token blacklisted for forced re-login.");
        } catch (Exception e) {
            log.warn("[AdminEmailChange] Could not blacklist token: {}", e.getMessage());
        }
    }

    /**
     * Sends post-change notification emails to both the old and new email addresses.
     * Errors here are logged but do not roll back the transaction.
     */
    private void sendChangeNotificationEmails(String oldEmail, String newEmail, String firstName) {
        String displayName = firstName != null ? firstName : "Admin";
        try {
            emailService.sendEmailChangeNotification(
                    oldEmail,
                    "Your CyberLearnix admin email address has been changed",
                    "Hi " + displayName + ",\n\n"
                  + "This is a security notification to inform you that the email address "
                  + "associated with your CyberLearnix admin account has been successfully changed "
                  + "to: " + newEmail + "\n\n"
                  + "If you did not make this change, please contact support immediately.\n\n"
                  + "— The CyberLearnix Team");
        } catch (Exception e) {
            log.error("[AdminEmailChange] Failed to send notification to old email {}: {}",
                    maskEmail(oldEmail), e.getMessage());
        }

        try {
            emailService.sendEmailChangeNotification(
                    newEmail,
                    "Your new admin email address is verified — CyberLearnix",
                    "Hi " + displayName + ",\n\n"
                  + "Your new email address has been successfully verified and is now the primary "
                  + "login email for your CyberLearnix admin account.\n\n"
                  + "Please use this email address when logging in from now on.\n\n"
                  + "— The CyberLearnix Team");
        } catch (Exception e) {
            log.error("[AdminEmailChange] Failed to send confirmation to new email {}: {}",
                    maskEmail(newEmail), e.getMessage());
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
        String[] parts   = email.split("@");
        String   local   = parts[0];
        String   visible = local.length() > 2 ? local.substring(0, 2) : local.substring(0, 1);
        return visible + "***@" + parts[1];
    }
}
