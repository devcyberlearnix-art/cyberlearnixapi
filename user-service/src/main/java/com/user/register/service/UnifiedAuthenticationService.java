package com.user.register.service;

import com.user.register.dto.unified.*;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.security.UnifiedJwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UnifiedJwtService unifiedJwtService;
    private final RestTemplate restTemplate;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${admin.service.url:http://localhost:8087}")
    private String adminServiceUrl;

    public String extractEmailFromToken(String token) {
        return unifiedJwtService.extractEmail(token);
    }

    public ResponseEntity<LoginResponse> login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail();
        String password = request.getPassword();

        // Try to find user in user database first
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            return handleUserLogin(userOptional.get(), password, httpRequest);
        }

        // If not found in user database, try admin login
        return tryAdminLogin(email, password, httpRequest);
    }

    private ResponseEntity<LoginResponse> handleUserLogin(User user, String password, HttpServletRequest httpRequest) {
        // Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // Check account status
        if (user.getStatus() == User.Status.PENDING_VERIFICATION) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email not verified");
        }
        if (user.getStatus() == User.Status.LOCKED) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Account locked due to too many failed login attempts");
        }
        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account suspended by admin");
        }

        // Check instructor approval
        if (user.getRole() == User.Role.INSTRUCTOR && !user.getIsInstructorApproved()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Instructor account not approved yet");
        }

        // Generate tokens
        String accessToken = unifiedJwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                null,
                null
        );

        String refreshToken = unifiedJwtService.generateRefreshToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name()
        );

        // Build response
        LoginResponse.UserData userData = LoginResponse.UserData.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(decryptField(user.getFirstName()))
                .lastName(decryptField(user.getLastName()))
                .mobileNumber(decryptField(user.getMobile()))
                .role(user.getRole().name())
                .adminType("NONE")
                .assignedService("NONE")
                .permissions(getPermissionsForRole(user.getRole().name()))
                .verified(user.getStatus() != User.Status.PENDING_VERIFICATION)
                .approved(user.getIsInstructorApproved() != null ? user.getIsInstructorApproved() : true)
                .build();

        LoginResponse.AuthenticationInfo authInfo = LoginResponse.AuthenticationInfo.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn("15 minutes")
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn("30 days")
                .build();

        LoginResponse.SessionInfo sessionInfo = buildSessionInfo(httpRequest);

        LoginResponse response = LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .user(userData)
                .authentication(authInfo)
                .sessionInfo(sessionInfo)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<LoginResponse> tryAdminLogin(String email, String password, HttpServletRequest httpRequest) {
        try {
            Map<String, Object> adminRequest = new HashMap<>();
            adminRequest.put("email", email);
            adminRequest.put("password", password);

            String adminLoginUrl = adminServiceUrl + "/api/v1/admins/login";
            ResponseEntity<Map> adminResponse = restTemplate.postForEntity(adminLoginUrl, adminRequest, Map.class);

            if (adminResponse.getStatusCode() == HttpStatus.OK && adminResponse.getBody() != null) {
                Map<String, Object> adminBody = adminResponse.getBody();
                return convertAdminResponseToUnified(adminBody, httpRequest);
            }
        } catch (Exception e) {
            log.error("Admin login failed for email: {}", email, e);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    private ResponseEntity<LoginResponse> convertAdminResponseToUnified(Map<String, Object> adminBody, HttpServletRequest httpRequest) {
        Map<String, Object> adminData = (Map<String, Object>) adminBody.get("admin");
        Map<String, Object> sessionData = (Map<String, Object>) adminBody.get("sessionInfo");

        if (adminData == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid admin response format");
        }

        String adminId = (String) adminData.get("id");
        String email = (String) adminData.get("email");
        String role = (String) adminData.get("role");
        String adminType = (String) adminData.get("adminType");
        String assignedService = (String) adminData.get("assignedService");

        // Generate JWT tokens using User Service's unified JWT service
        String accessToken = unifiedJwtService.generateAccessToken(
                adminId,
                email,
                role,
                adminType,
                assignedService
        );

        String refreshToken = unifiedJwtService.generateRefreshToken(
                adminId,
                email,
                role
        );

        LoginResponse.UserData userData = LoginResponse.UserData.builder()
                .id(UUID.fromString(adminId))
                .email(email)
                .firstName((String) adminData.get("firstName"))
                .lastName((String) adminData.get("lastName"))
                .mobileNumber((String) adminData.get("mobileNumber"))
                .role(role)
                .adminType(adminType)
                .assignedService(assignedService)
                .permissions(getPermissionsForRole(role, adminType))
                .verified(true)
                .approved(true)
                .build();

        LoginResponse.AuthenticationInfo authInfo = LoginResponse.AuthenticationInfo.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn("15m")
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn("30d")
                .build();

        LoginResponse.SessionInfo sessionInfo = LoginResponse.SessionInfo.builder()
                .loginTime((String) sessionData.get("loginTime"))
                .ipAddress((String) sessionData.get("ipAddress"))
                .device((String) sessionData.get("device"))
                .build();

        LoginResponse response = LoginResponse.builder()
                .success(true)
                .message("Admin login successful")
                .user(userData)
                .authentication(authInfo)
                .sessionInfo(sessionInfo)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<RefreshTokenResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!unifiedJwtService.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        if (unifiedJwtService.isTokenExpired(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        String userId = unifiedJwtService.extractUserId(refreshToken);
        String email = unifiedJwtService.extractEmail(refreshToken);
        String role = unifiedJwtService.extractRole(refreshToken);

        // Generate new tokens
        String newAccessToken = unifiedJwtService.generateAccessToken(userId, email, role, null, null);
        String newRefreshToken = unifiedJwtService.generateRefreshToken(userId, email, role);

        // Blacklist old refresh token
        tokenBlacklistService.blacklistToken(refreshToken);

        RefreshTokenResponse.AuthenticationInfo authInfo = RefreshTokenResponse.AuthenticationInfo.builder()
                .accessToken(newAccessToken)
                .accessTokenExpiresIn("15 minutes")
                .refreshToken(newRefreshToken)
                .refreshTokenExpiresIn("30 days")
                .build();

        RefreshTokenResponse response = RefreshTokenResponse.builder()
                .success(true)
                .message("Token refreshed successfully")
                .authentication(authInfo)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> logout(String accessToken, String refreshToken) {
        // Blacklist both tokens
        if (accessToken != null) {
            tokenBlacklistService.blacklistToken(accessToken);
        }
        if (refreshToken != null) {
            tokenBlacklistService.blacklistToken(refreshToken);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    private LoginResponse.SessionInfo buildSessionInfo(HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        String userAgent = httpRequest.getHeader("User-Agent");
        String device = detectDevice(userAgent);

        return LoginResponse.SessionInfo.builder()
                .loginTime(LocalDateTime.now().toString())
                .ipAddress(ipAddress)
                .device(device)
                .build();
    }

    private String detectDevice(String userAgent) {
        if (userAgent == null) return "Unknown Device";
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("postman")) return "Postman";
        if (userAgent.contains("android")) return "Android Mobile";
        if (userAgent.contains("iphone")) return "iPhone";
        if (userAgent.contains("ipad")) return "iPad";
        if (userAgent.contains("windows")) return "Windows Desktop";
        if (userAgent.contains("mac")) return "Mac Desktop";
        if (userAgent.contains("linux")) return "Linux Desktop";

        return "Unknown Device";
    }

    private List<String> getPermissionsForRole(String role) {
        List<String> permissions = new ArrayList<>();
        switch (role) {
            case "STUDENT":
                permissions.addAll(Arrays.asList("course:read", "enrollment:create", "review:create"));
                break;
            case "INSTRUCTOR":
                permissions.addAll(Arrays.asList("course:create", "course:update", "course:delete", "lecture:create", "lecture:update", "lecture:delete"));
                break;
            case "MAIN_ADMIN":
                permissions.addAll(Arrays.asList("admin:all", "user:manage", "course:manage", "payment:manage", "report:view"));
                break;
            case "SUB_ADMIN":
                permissions.addAll(Arrays.asList("user:view", "course:view", "payment:view"));
                break;
        }
        return permissions;
    }

    private List<String> getPermissionsForRole(String role, String adminType) {
        if ("MAIN_ADMIN".equals(adminType)) {
            return Arrays.asList("admin:all", "user:manage", "course:manage", "payment:manage", "report:view");
        } else if ("SUB_ADMIN".equals(adminType)) {
            return Arrays.asList("user:view", "course:view", "payment:view");
        }
        return getPermissionsForRole(role);
    }

    private String decryptField(String encrypted) {
        if (encrypted == null) return "";
        try {
            return com.user.register.util.SecurityUtils.decrypt(encrypted, "1234567890123456");
        } catch (Exception e) {
            return encrypted;
        }
    }

    public ResponseEntity<Map<String, Object>> forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email is required");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }

        String email = request.getEmail();

        // Check if user exists in user database
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // Generate OTP for password reset
            String otp = generateOTP();
            // TODO: Store OTP in database with expiration
            // TODO: Send OTP via email
            log.info("Password reset OTP sent to user: {}", email);
        } else {
            // Try admin service
            try {
                if (adminServiceUrl != null && restTemplate != null) {
                    Map<String, Object> adminRequest = new HashMap<>();
                    adminRequest.put("email", email);
                    String adminUrl = adminServiceUrl + "/api/v1/admins/password/forgot";
                    restTemplate.postForEntity(adminUrl, adminRequest, Map.class);
                    log.info("Password reset OTP sent to admin: {}", email);
                }
            } catch (Exception e) {
                log.error("Failed to send password reset OTP to admin: {}", email, e);
            }
        }

        // Always return success to prevent email enumeration
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "If the email exists, a password reset OTP has been sent");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> verifyPasswordOtp(VerifyOtpRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();

        // TODO: Verify OTP from database
        boolean isValid = verifyOtp(email, otp, "password_reset");

        if (!isValid) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid or expired OTP");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP verified successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> changePassword(ChangePasswordRequest request, String email) {
        if (request == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Request body is required");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }

        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (currentPassword == null || newPassword == null || confirmPassword == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Current password, new password, and confirm password are required");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }

        if (!newPassword.equals(confirmPassword)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "New password and confirm password do not match");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (email == null || email.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email is required");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }

        // Check if user exists in user database
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Password changed for user: {}", email);
        } else {
            // Try admin service
            try {
                if (adminServiceUrl != null && restTemplate != null) {
                    Map<String, Object> adminRequest = new HashMap<>();
                    adminRequest.put("email", email);
                    adminRequest.put("currentPassword", currentPassword);
                    adminRequest.put("newPassword", newPassword);
                    adminRequest.put("confirmPassword", confirmPassword);
                    String adminUrl = adminServiceUrl + "/api/v1/admins/change-password";
                    restTemplate.postForEntity(adminUrl, adminRequest, Map.class);
                    log.info("Password changed for admin: {}", email);
                }
            } catch (Exception e) {
                log.error("Failed to change password for admin: {}", email, e);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to change password");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password changed successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> resetPassword(ResetPasswordRequest request, String email) {
        if (request == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Request body is required");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }

        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (newPassword == null || confirmPassword == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "New password and confirm password are required");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }

        if (!newPassword.equals(confirmPassword)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Passwords do not match");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (email == null || email.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email is required");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }

        // Check if user exists in user database
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Password reset for user: {}", email);
        } else {
            // Try admin service
            try {
                if (adminServiceUrl != null && restTemplate != null) {
                    Map<String, Object> adminRequest = new HashMap<>();
                    adminRequest.put("email", email);
                    adminRequest.put("newPassword", newPassword);
                    adminRequest.put("confirmPassword", confirmPassword);
                    String adminUrl = adminServiceUrl + "/api/v1/admins/password/reset";
                    restTemplate.postForEntity(adminUrl, adminRequest, Map.class);
                    log.info("Password reset for admin: {}", email);
                }
            } catch (Exception e) {
                log.error("Failed to reset password for admin: {}", email, e);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to reset password");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> requestLoginOtp(RequestOtpRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email is required");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
        }

        String email = request.getEmail();

        // Check if user exists in user database
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // Generate OTP for login
            String otp = generateOTP();
            // TODO: Store OTP in database with expiration
            // TODO: Send OTP via email
            log.info("Login OTP sent to user: {}", email);
        } else {
            // Try admin service
            try {
                if (adminServiceUrl != null && restTemplate != null) {
                    Map<String, Object> adminRequest = new HashMap<>();
                    adminRequest.put("email", email);
                    String adminUrl = adminServiceUrl + "/api/v1/admins/login/otp/request";
                    restTemplate.postForEntity(adminUrl, adminRequest, Map.class);
                    log.info("Login OTP sent to admin: {}", email);
                }
            } catch (Exception e) {
                log.error("Failed to send login OTP to admin: {}", email, e);
            }
        }

        // Always return success to prevent email enumeration
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "If the email exists, a login OTP has been sent");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<LoginResponse> verifyLoginOtp(VerifyOtpRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail();
        String otp = request.getOtp();

        // TODO: Verify OTP from database
        boolean isValid = verifyOtp(email, otp, "login");

        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        // After OTP verification, proceed with login
        // For now, we'll need to fetch the user/admin and generate tokens
        // This is a simplified version - you may need to adjust based on your OTP flow
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            return handleUserLogin(userOptional.get(), null, httpRequest); // Password not needed for OTP login
        }

        // Try admin login without password (OTP-based)
        return tryAdminOtpLogin(email, httpRequest);
    }

    private ResponseEntity<LoginResponse> tryAdminOtpLogin(String email, HttpServletRequest httpRequest) {
        try {
            Map<String, Object> adminRequest = new HashMap<>();
            adminRequest.put("email", email);
            String adminUrl = adminServiceUrl + "/api/v1/admins/login/otp/verify";
            ResponseEntity<Map> adminResponse = restTemplate.postForEntity(adminUrl, adminRequest, Map.class);

            if (adminResponse.getStatusCode() == HttpStatus.OK && adminResponse.getBody() != null) {
                return convertAdminResponseToUnified(adminResponse.getBody(), httpRequest);
            }
        } catch (Exception e) {
            log.error("Admin OTP login failed for email: {}", email, e);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP or email");
    }

    private String generateOTP() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private boolean verifyOtp(String email, String otp, String type) {
        // TODO: Implement actual OTP verification from database
        // For now, return false as placeholder
        return false;
    }
}
