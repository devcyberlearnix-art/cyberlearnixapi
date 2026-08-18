package com.example.admin.service;

import com.example.admin.dto.AdminPasswordChangeDto;
import com.example.admin.dto.AdminPasswordChangeResponse;
import com.example.admin.dto.VerifyAdminPasswordOtpDto;
import com.example.admin.entity.Admin;
import com.example.admin.entity.AdminPasswordChangeAuditLog;
import com.example.admin.entity.AdminPasswordHistory;
import com.example.admin.entity.AdminPasswordOtp;
import com.example.admin.exception.AdminPasswordChangeException;
import com.example.admin.repository.AdminPasswordChangeAuditLogRepository;
import com.example.admin.repository.AdminPasswordHistoryRepository;
import com.example.admin.repository.AdminPasswordOtpRepository;
import com.example.admin.repository.AdminRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPasswordChangeService {

    private final AdminRepository adminRepository;
    private final AdminPasswordHistoryRepository adminPasswordHistoryRepository;
    private final AdminPasswordOtpRepository adminPasswordOtpRepository;
    private final AdminPasswordChangeAuditLogRepository adminPasswordChangeAuditLogRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final String ADMIN_PWD_CHANGE_PREFIX = "ADMIN:PASSWORD_CHANGE_TIME:";
    private static final String TOKEN_BLACKLIST_PREFIX = "ADMIN:JWT:BLACKLIST:";

    // Strong password pattern: Min 12 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special character
    private static final Pattern PWD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$"
    );

    @Transactional
    public AdminPasswordChangeResponse initiatePasswordChange(HttpServletRequest request, UUID adminId, AdminPasswordChangeDto dto) {
        log.info("[AdminPasswordChange] Initiating password change request for adminId={}", adminId);

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> AdminPasswordChangeException.notFound("Admin not found."));

        // Check if account is locked
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("[AdminPasswordChange] Account is locked until {} for adminId={}", admin.getLockedUntil(), adminId);
            throw AdminPasswordChangeException.unauthorized("Your account is temporarily locked due to too many failed password verification attempts. Please try again later.");
        }

        // Validate old password
        if (!passwordEncoder.matches(dto.getOldPassword(), admin.getPassword())) {
            int attempts = (admin.getFailedPasswordAttempts() != null ? admin.getFailedPasswordAttempts() : 0) + 1;
            admin.setFailedPasswordAttempts(attempts);
            
            // Log audit log of failure
            saveAuditLog(adminId, "PASSWORD_CHANGE_INITIATE", request.getRemoteAddr(), getClientDevice(request), "FAILURE");

            if (attempts >= 5) {
                admin.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                admin.setFailedPasswordAttempts(0); // reset after lock
                adminRepository.save(admin);
                log.warn("[AdminPasswordChange] Max attempts reached. Account locked for 15 minutes. adminId={}", adminId);
                throw AdminPasswordChangeException.unauthorized("Incorrect old password. Your account has been temporarily locked for 15 minutes.");
            }

            adminRepository.save(admin);
            log.warn("[AdminPasswordChange] Incorrect old password verification for adminId={}, attempt {}/5", adminId, attempts);
            throw AdminPasswordChangeException.unauthorized("Incorrect old password. Remaining attempts before lockout: " + (5 - attempts));
        }

        // Validate input fields
        if (dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            throw AdminPasswordChangeException.badRequest("New password and confirm password fields are required.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw AdminPasswordChangeException.badRequest("New password and confirm password do not match.");
        }

        // Enforce strong password policy
        if (!PWD_PATTERN.matcher(dto.getNewPassword()).matches()) {
            throw AdminPasswordChangeException.badRequest("New password does not meet the strong password policy requirements. It must contain at least 12 characters, including one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&).");
        }

        // Password reuse check: Must not match current password
        if (passwordEncoder.matches(dto.getNewPassword(), admin.getPassword())) {
            throw AdminPasswordChangeException.badRequest("New password cannot be the same as your current password.");
        }

        // Password reuse check: Must not match last 5 historical passwords
        List<AdminPasswordHistory> histories = adminPasswordHistoryRepository.findRecentByAdminId(adminId, PageRequest.of(0, 5));
        for (AdminPasswordHistory history : histories) {
            if (passwordEncoder.matches(dto.getNewPassword(), history.getPasswordHash())) {
                throw AdminPasswordChangeException.badRequest("New password matches one of your last 5 previous passwords. Please choose a different password.");
            }
        }

        // Expire any existing pending password change requests for this admin
        adminPasswordOtpRepository.expirePendingOtpSessionsForAdmin(adminId);

        // Generate and Hash OTP
        String rawOtp = generateNumericOtp();
        String hashedOtp = hashSha256(rawOtp);
        String hashedNewPassword = passwordEncoder.encode(dto.getNewPassword());

        // Save verification session
        AdminPasswordOtp adminPasswordOtp = AdminPasswordOtp.builder()
                .adminId(adminId)
                .otpHash(hashedOtp)
                .newPasswordHash(hashedNewPassword)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .status(AdminPasswordOtp.Status.PENDING)
                .build();

        AdminPasswordOtp savedOtp = adminPasswordOtpRepository.save(adminPasswordOtp);

        // Send OTP
        emailService.sendPasswordChangeOtp(admin.getEmail(), rawOtp);

        log.info("[AdminPasswordChange] Password change request initiated successfully. sessionId={}", savedOtp.getId());

        return AdminPasswordChangeResponse.builder()
                .sessionId(savedOtp.getId().toString())
                .status("PENDING")
                .expiresAt(savedOtp.getExpiryTime())
                .message("OTP sent to your registered email. Please verify within 5 minutes.")
                .build();
    }

    @Transactional
    public AdminPasswordChangeResponse verifyPasswordOtp(HttpServletRequest request, UUID adminId, VerifyAdminPasswordOtpDto dto) {
        log.info("[AdminPasswordChange] Verifying OTP for session={}", dto.getSessionId());

        UUID sessionUuid;
        try {
            sessionUuid = UUID.fromString(dto.getSessionId());
        } catch (IllegalArgumentException e) {
            throw AdminPasswordChangeException.badRequest("Invalid session ID format.");
        }

        AdminPasswordOtp adminPasswordOtp = adminPasswordOtpRepository.findByIdAndAdminId(sessionUuid, adminId)
                .orElseThrow(() -> AdminPasswordChangeException.notFound("Password change session not found or access denied."));

        // Validate state
        if (adminPasswordOtp.getStatus() != AdminPasswordOtp.Status.PENDING) {
            throw AdminPasswordChangeException.badRequest("This session is no longer active. Current status: " + adminPasswordOtp.getStatus());
        }

        if (adminPasswordOtp.isExpired()) {
            adminPasswordOtp.setStatus(AdminPasswordOtp.Status.EXPIRED);
            adminPasswordOtpRepository.save(adminPasswordOtp);
            throw AdminPasswordChangeException.gone("This OTP has expired. Please initiate a new password change request.");
        }

        if (adminPasswordOtp.getAttempts() >= 3) {
            adminPasswordOtp.setStatus(AdminPasswordOtp.Status.EXPIRED);
            adminPasswordOtpRepository.save(adminPasswordOtp);
            throw AdminPasswordChangeException.tooManyRequests("Maximum OTP verification attempts (3) exceeded. Please request a new OTP code.");
        }

        // Verify OTP hash
        String submittedHash = hashSha256(dto.getOtp());
        if (!adminPasswordOtp.getOtpHash().equals(submittedHash)) {
            int newAttempts = adminPasswordOtp.getAttempts() + 1;
            adminPasswordOtp.setAttempts(newAttempts);
            
            if (newAttempts >= 3) {
                adminPasswordOtp.setStatus(AdminPasswordOtp.Status.EXPIRED);
                adminPasswordOtpRepository.save(adminPasswordOtp);
                log.warn("[AdminPasswordChange] Max OTP attempts reached. Session invalidated. sessionId={}", sessionUuid);
                throw AdminPasswordChangeException.tooManyRequests("Incorrect OTP. Maximum attempts exceeded. This session has been invalidated.");
            }
            
            adminPasswordOtpRepository.save(adminPasswordOtp);
            log.warn("[AdminPasswordChange] Incorrect OTP for session={}, attempt {}/3", sessionUuid, newAttempts);
            throw AdminPasswordChangeException.unauthorized("Incorrect OTP. Remaining attempts: " + (3 - newAttempts));
        }

        // OTP Valid -> Complete update
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> AdminPasswordChangeException.notFound("Admin not found."));

        // Add old password to history
        AdminPasswordHistory history = AdminPasswordHistory.builder()
                .adminId(adminId)
                .passwordHash(admin.getPassword())
                .createdAt(LocalDateTime.now())
                .build();
        adminPasswordHistoryRepository.save(history);

        // Update admin password and reset lockout/failed states
        admin.setPassword(adminPasswordOtp.getNewPasswordHash());
        admin.setFailedPasswordAttempts(0);
        admin.setLockedUntil(null);
        adminRepository.save(admin);

        // Update verification session state
        adminPasswordOtp.setStatus(AdminPasswordOtp.Status.VERIFIED);
        adminPasswordOtpRepository.save(adminPasswordOtp);

        // Blacklist current access token in Redis
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String currentJwt = authHeader.substring(7).trim();
                // Expire in 15 minutes (default admin token lifetime)
                redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + currentJwt, "true", java.time.Duration.ofMinutes(15));
            }
        } catch (Exception e) {
            log.error("[AdminPasswordChange] Error blacklisting current JWT token: {}", e.getMessage());
        }

        // Write password change timestamp to Redis (invalidates all other devices stateless tokens)
        long nowMs = System.currentTimeMillis();
        redisTemplate.opsForValue().set(ADMIN_PWD_CHANGE_PREFIX + adminId, String.valueOf(nowMs));
        log.info("[AdminPasswordChange] Invalidated all previous JWT tokens by writing timestamp {} to Redis", nowMs);

        // Audit Logging
        saveAuditLog(adminId, "PASSWORD_CHANGE", request.getRemoteAddr(), getClientDevice(request), "SUCCESS");

        // Send confirmation email
        emailService.sendPasswordChangeNotification(
                admin.getEmail(),
                LocalDateTime.now().toString(),
                request.getRemoteAddr(),
                getClientDevice(request)
        );

        log.info("[AdminPasswordChange] Password successfully changed for adminId={}", adminId);

        return AdminPasswordChangeResponse.builder()
                .sessionId(adminPasswordOtp.getId().toString())
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

    private void saveAuditLog(UUID adminId, String action, String ipAddress, String device, String status) {
        try {
            AdminPasswordChangeAuditLog auditLog = AdminPasswordChangeAuditLog.builder()
                    .adminId(adminId)
                    .action(action)
                    .ipAddress(ipAddress)
                    .device(device)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();
            adminPasswordChangeAuditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("[AdminPasswordChange] Failed to save audit log: {}", e.getMessage());
        }
    }

    private String getClientDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown Device";
    }
}
