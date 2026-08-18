package com.user.register.service;

import com.user.register.dto.PasswordChangeDto;
import com.user.register.dto.PasswordChangeResponse;
import com.user.register.dto.VerifyPasswordOtpDto;
import com.user.register.entity.AuditLog;
import com.user.register.entity.PasswordHistory;
import com.user.register.entity.PasswordOtp;
import com.user.register.entity.User;
import com.user.register.exception.PasswordChangeException;
import com.user.register.repository.AuditLogRepository;
import com.user.register.repository.PasswordHistoryRepository;
import com.user.register.repository.PasswordOtpRepository;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeService {

    private final UserRepository userRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordOtpRepository passwordOtpRepository;
    private final UserSessionRepository sessionRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final String USER_PWD_CHANGE_PREFIX = "USER:PASSWORD_CHANGE_TIME:";

    // Strong password pattern: Min 12 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special character
    private static final Pattern PWD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$"
    );

    @Transactional
    public PasswordChangeResponse initiatePasswordChange(HttpServletRequest request, UUID userId, PasswordChangeDto dto) {
        log.info("[PasswordChange] Initiating password change request for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> PasswordChangeException.notFound("User not found."));

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("[PasswordChange] Account is locked until {} for userId={}", user.getLockedUntil(), userId);
            throw PasswordChangeException.unauthorized("Your account is temporarily locked due to too many failed password verification attempts. Please try again later.");
        }

        // Validate old password
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() + 1 : 1;
            user.setFailedLoginAttempts(attempts);
            
            // Log audit log of failure
            saveAuditLog(user, "PASSWORD_CHANGE_INITIATE", request.getRemoteAddr(), getClientDevice(request), "FAILURE");

            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                user.setFailedLoginAttempts(0); // reset after lock
                userRepository.save(user);
                log.warn("[PasswordChange] Max attempts reached. Account locked for 15 minutes. userId={}", userId);
                throw PasswordChangeException.unauthorized("Incorrect old password. Your account has been temporarily locked for 15 minutes.");
            }

            userRepository.save(user);
            log.warn("[PasswordChange] Incorrect old password verification for userId={}, attempt {}/5", userId, attempts);
            throw PasswordChangeException.unauthorized("Incorrect old password. Remaining attempts before lockout: " + (5 - attempts));
        }

        // Validate input fields
        if (dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            throw PasswordChangeException.badRequest("New password and confirm password fields are required.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw PasswordChangeException.badRequest("New password and confirm password do not match.");
        }

        // Enforce strong password policy
        if (!PWD_PATTERN.matcher(dto.getNewPassword()).matches()) {
            throw PasswordChangeException.badRequest("New password does not meet the strong password policy requirements. It must contain at least 12 characters, including one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&).");
        }

        // Password reuse check: Must not match current password
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw PasswordChangeException.badRequest("New password cannot be the same as your current password.");
        }

        // Password reuse check: Must not match last 5 historical passwords
        List<PasswordHistory> histories = passwordHistoryRepository.findRecentByUserId(userId, PageRequest.of(0, 5));
        for (PasswordHistory history : histories) {
            if (passwordEncoder.matches(dto.getNewPassword(), history.getPasswordHash())) {
                throw PasswordChangeException.badRequest("New password matches one of your last 5 previous passwords. Please choose a different password.");
            }
        }

        // Expire any existing pending password change requests for this user
        passwordOtpRepository.expirePendingOtpSessionsForUser(userId);

        // Generate and Hash OTP
        String rawOtp = generateNumericOtp();
        String hashedOtp = hashSha256(rawOtp);
        String hashedNewPassword = passwordEncoder.encode(dto.getNewPassword());

        // Save verification session
        PasswordOtp passwordOtp = PasswordOtp.builder()
                .userId(userId)
                .otpHash(hashedOtp)
                .newPasswordHash(hashedNewPassword)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .status(PasswordOtp.Status.PENDING)
                .build();

        PasswordOtp savedOtp = passwordOtpRepository.save(passwordOtp);

        // Send OTP
        emailService.sendPasswordChangeOtp(user.getEmail(), rawOtp);

        log.info("[PasswordChange] Password change request initiated successfully. sessionId={}", savedOtp.getId());

        return PasswordChangeResponse.builder()
                .sessionId(savedOtp.getId().toString())
                .status("PENDING")
                .expiresAt(savedOtp.getExpiryTime())
                .message("OTP sent to your registered email. Please verify within 5 minutes.")
                .build();
    }

    @Transactional
    public PasswordChangeResponse verifyPasswordOtp(HttpServletRequest request, UUID userId, VerifyPasswordOtpDto dto) {
        log.info("[PasswordChange] Verifying OTP for session={}", dto.getSessionId());

        UUID sessionUuid;
        try {
            sessionUuid = UUID.fromString(dto.getSessionId());
        } catch (IllegalArgumentException e) {
            throw PasswordChangeException.badRequest("Invalid session ID format.");
        }

        PasswordOtp passwordOtp = passwordOtpRepository.findByIdAndUserId(sessionUuid, userId)
                .orElseThrow(() -> PasswordChangeException.notFound("Password change session not found or access denied."));

        // Validate state
        if (passwordOtp.getStatus() != PasswordOtp.Status.PENDING) {
            throw PasswordChangeException.badRequest("This session is no longer active. Current status: " + passwordOtp.getStatus());
        }

        if (passwordOtp.isExpired()) {
            passwordOtp.setStatus(PasswordOtp.Status.EXPIRED);
            passwordOtpRepository.save(passwordOtp);
            throw PasswordChangeException.gone("This OTP has expired. Please initiate a new password change request.");
        }

        if (passwordOtp.getAttempts() >= 3) {
            passwordOtp.setStatus(PasswordOtp.Status.EXPIRED);
            passwordOtpRepository.save(passwordOtp);
            throw PasswordChangeException.tooManyRequests("Maximum OTP verification attempts (3) exceeded. Please request a new OTP code.");
        }

        // Verify OTP hash
        String submittedHash = hashSha256(dto.getOtp());
        if (!passwordOtp.getOtpHash().equals(submittedHash)) {
            int newAttempts = passwordOtp.getAttempts() + 1;
            passwordOtp.setAttempts(newAttempts);
            
            if (newAttempts >= 3) {
                passwordOtp.setStatus(PasswordOtp.Status.EXPIRED);
                passwordOtpRepository.save(passwordOtp);
                log.warn("[PasswordChange] Max OTP attempts reached. Session invalidated. sessionId={}", sessionUuid);
                throw PasswordChangeException.tooManyRequests("Incorrect OTP. Maximum attempts exceeded. This session has been invalidated.");
            }
            
            passwordOtpRepository.save(passwordOtp);
            log.warn("[PasswordChange] Incorrect OTP for session={}, attempt {}/3", sessionUuid, newAttempts);
            throw PasswordChangeException.unauthorized("Incorrect OTP. Remaining attempts: " + (3 - newAttempts));
        }

        // OTP Valid -> Complete update
        User user = userRepository.findById(userId)
                .orElseThrow(() -> PasswordChangeException.notFound("User not found."));

        // Add old password to history
        PasswordHistory history = PasswordHistory.builder()
                .userId(userId)
                .passwordHash(user.getPassword())
                .createdAt(LocalDateTime.now())
                .build();
        passwordHistoryRepository.save(history);

        // Update user password and reset lockout/failed states
        user.setPassword(passwordOtp.getNewPasswordHash());
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Update verification session state
        passwordOtp.setStatus(PasswordOtp.Status.VERIFIED);
        passwordOtpRepository.save(passwordOtp);

        // Revoke active JWTs & sessions (logout from all devices)
        try {
            sessionRepository.deleteByUser(user);
            // Blacklist the current JWT if we can get it from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String currentJwt = authHeader.substring(7);
                tokenBlacklistService.blacklistToken(currentJwt);
            }
        } catch (Exception e) {
            log.error("[PasswordChange] Error deleting user sessions: {}", e.getMessage());
        }

        // Write password change timestamp to Redis (invalidates all existing access tokens)
        long nowMs = System.currentTimeMillis();
        redisTemplate.opsForValue().set(USER_PWD_CHANGE_PREFIX + userId, String.valueOf(nowMs));
        log.info("[PasswordChange] Invalidated all previous JWT tokens by writing timestamp {} to Redis", nowMs);

        // Audit Logging
        saveAuditLog(user, "PASSWORD_CHANGE", request.getRemoteAddr(), getClientDevice(request), "SUCCESS");

        // Send confirmation email
        emailService.sendPasswordChangeNotification(
                user.getEmail(),
                LocalDateTime.now().toString(),
                request.getRemoteAddr(),
                getClientDevice(request)
        );

        log.info("[PasswordChange] Password successfully changed for userId={}", userId);

        return PasswordChangeResponse.builder()
                .sessionId(passwordOtp.getId().toString())
                .status("VERIFIED")
                .requiresReLogin(true)
                .message("Password changed successfully. You have been logged out of all devices. Please login again.")
                .build();
    }

    private String generateNumericOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private String hashSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void saveAuditLog(User user, String action, String ipAddress, String device, String status) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .ipAddress(ipAddress)
                    .device(device)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("[PasswordChange] Failed to save audit log: {}", e.getMessage());
        }
    }

    private String getClientDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown Device";
    }
}
