package com.example.admin.service;

import com.example.admin.audit.AuditService;
import com.example.admin.dto.*;
import com.example.admin.entity.*;
import com.example.admin.repository.AdminRepository;
import com.example.admin.security.AdminPrincipal;
import com.example.admin.security.AdminSecurityContext;
import com.example.admin.security.JwtService;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final EncryptionService encryptionService;
    private final EmailService emailService;
    private final AdminPermissionService adminPermissionService;
    private String otp;

    public AdminLoginResponse login(AdminLoginRequest request,
                                    HttpServletRequest httpRequest) {

        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (admin.getAdminType() == com.example.admin.entity.AdminType.SUB_ADMIN) {
            if (admin.getApprovalStatus() != com.example.admin.entity.AdminApprovalStatus.APPROVED) {
                throw new BadCredentialsException("Sub Admin account is not approved yet");
            }
            if (!admin.isVerified()) {
                throw new BadCredentialsException("Sub Admin account is not active");
            }
        }

        // Return admin data without generating tokens (User Service will generate tokens)
        String firstName = decryptSafely(admin.getFirstName(), "Admin");
        String lastName = decryptSafely(admin.getLastName(), "");
        String mobileNumber = decryptSafely(admin.getMobileNumber(), "");

        return AdminLoginResponse.builder()
                .admin(AdminLoginResponse.AdminInfo.builder()
                        .id(admin.getId())
                        .email(admin.getEmail())
                        .role(admin.getRole())
                        .assignedService(admin.getAssignedService() != null ? admin.getAssignedService().name() : "ALL")
                        .adminType(admin.getAdminType() != null ? admin.getAdminType().name() : "MAIN_ADMIN")
                        .firstName(firstName)
                        .lastName(lastName)
                        .mobileNumber(mobileNumber)
                        .build())
                .authentication(AdminLoginResponse.AuthenticationInfo.builder()
                        .accessToken("") // Empty string instead of null to avoid JSON issues
                        .accessTokenExpiresIn("")
                        .refreshToken("")
                        .refreshTokenExpiresIn("")
                        .build())
                .sessionInfo(AdminLoginResponse.SessionInfo.builder()
                        .loginTime(LocalDateTime.now().toString())
                        .ipAddress(httpRequest.getRemoteAddr())
                        .device(httpRequest.getHeader("User-Agent"))
                        .build())
                .build();
    }

    public AdminLoginResponse loginWithOtp(AdminLoginRequest request,
                                            String otp,
                                            HttpServletRequest httpRequest) {
        // Admin login should now go through User Service
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Admin login should be performed through User Service at /api/v1/auth/login");
    }

    public AdminProfileResponse updateProfile(UUID adminId,
                                              UpdateAdminProfileRequest request,
                                              HttpServletRequest httpRequest) {

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (request.getEmail() != null) admin.setEmail(request.getEmail());
        if (request.getPassword() != null) admin.setPassword(passwordEncoder.encode(request.getPassword()));

        adminRepository.save(admin);

        String ipAddress = httpRequest.getRemoteAddr();
        String device = httpRequest.getHeader("User-Agent");
        if (device == null) device = "Unknown";

        auditService.logAction(adminId, "ADMIN_PROFILE_UPDATED");
        return AdminProfileResponse.builder()
                .success(true)
                .message("Admin profile updated successfully")
                .timestamp(LocalDateTime.now().toString())
                .data(AdminProfileResponse.DataInfo.builder()
                        .admin(AdminProfileResponse.AdminInfo.builder()
                                .id(admin.getId())
                                .email(admin.getEmail())
                                .role(admin.getRole())
                                .build())
                        .ipAddress(ipAddress)
                        .device(device)
                        .build())
                .build();
    }

    public AdminProfileResponse getProfile(UUID adminId,
                                           HttpServletRequest httpRequest) {

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        String ipAddress = httpRequest.getRemoteAddr();

        if (ipAddress.equals("0:0:0:0:0:0:0:1")) {
            ipAddress = "127.0.0.1";
        }

        String device = httpRequest.getHeader("User-Agent");

        if (device == null) {
            device = "Unknown Device";
        }

        return AdminProfileResponse.builder()
                .success(true)
                .message("Admin profile fetched successfully")
                .timestamp(LocalDateTime.now().toString())
                .data(AdminProfileResponse.DataInfo.builder()
                        .admin(AdminProfileResponse.AdminInfo.builder()
                                .id(admin.getId())
                                .email(admin.getEmail())
                                .role(admin.getRole())
                                .build())
                        .ipAddress(ipAddress)
                        .device(device)
                        .build())
                .build();
    }

    public LogoutResponse logout(UUID adminId, HttpServletRequest httpRequest) {

        // Fetch admin from database
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        String ipAddress = httpRequest.getRemoteAddr();

        if (ipAddress.equals("0:0:0:0:0:0:0:1")) {
            ipAddress = "127.0.0.1";
        }

        String device = httpRequest.getHeader("User-Agent");

        if (device == null) {
            device = "Unknown Device";
        }

        auditService.logAction(adminId,
                "ADMIN_LOGOUT from IP: " + ipAddress + " Device: " + device);

        return LogoutResponse.builder()
                .success(true)
                .message("Admin logged out successfully")
                .timestamp(java.time.LocalDateTime.now().toString())
                .data(LogoutResponse.LogoutData.builder()
                        .admin(LogoutResponse.Admin.builder()
                                .id(admin.getId())
                                .email(admin.getEmail())
                                .role(admin.getRole())
                                .build())
                        .logoutSession(LogoutResponse.LogoutSession.builder()
                                .ipAddress(ipAddress)
                                .device(device)
                                .logoutTime(java.time.LocalDateTime.now().toString())
                                .sessionStatus("TERMINATED")
                                .build())
                        .build())
                .build();
    }

    /**
     * Main Admin only — registers a Sub Admin for one service.
     * Requires Authorization: Bearer &lt;main_admin_access_token&gt;
     */
    public AdminRegisterResponse registerAdmin(AdminRegisterRequest request) {
        AdminPrincipal principal = AdminSecurityContext.getPrincipal();
        adminPermissionService.requireMainAdmin(principal);

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return failure("email is required");
        }
        if (request.getAssignedService() == null || request.getAssignedService().isBlank()) {
            return failure("assignedService is required (e.g. ORDER_SERVICE)");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return failure("password is required for the new Sub Admin");
        }
        if (request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            return failure("Password and confirm password do not match");
        }

        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!request.getPassword().matches(passwordRegex)) {
            return failure("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
        }

        AssignedService assignedService;
        try {
            assignedService = AssignedService.valueOf(request.getAssignedService().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return failure("Invalid assignedService. Use ORDER_SERVICE, CART_SERVICE, PAYMENT_SERVICE, "
                    + "USER_SERVICE, COURSE_SERVICE, INSTRUCTOR_SERVICE");
        }
        if (assignedService == AssignedService.ALL) {
            return failure("Sub Admin cannot be assigned ALL services");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (adminRepository.findByEmail(email).isPresent()) {
            return failure("Email already registered");
        }

        String mobile = request.getMobileNumber() != null ? request.getMobileNumber() : "+911234567890";
        String altMobile = request.getAlternateMobileNumber() != null ? request.getAlternateMobileNumber() : mobile;
        String firstName = request.getFirstName() != null ? request.getFirstName() : "Sub";
        String lastName = request.getLastName() != null ? request.getLastName() : "Admin";

        Admin subAdmin = Admin.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("SUB_ADMIN")
                .adminType(AdminType.SUB_ADMIN)
                .assignedService(assignedService)
                .approvalStatus(AdminApprovalStatus.APPROVED)
                .verified(true)
                .approvedBy(principal.adminId())
                .firstName(encryptionService.encrypt(firstName))
                .lastName(encryptionService.encrypt(lastName))
                .mobileNumber(encryptionService.encrypt(mobile))
                .alternateMobileNumber(encryptionService.encrypt(altMobile))
                .build();

        adminRepository.save(subAdmin);
        auditService.logAction(principal.adminId(),
                "SUB_ADMIN_REGISTERED:" + subAdmin.getId() + ":" + assignedService.name());

        return AdminRegisterResponse.builder()
                .success(true)
                .message("Sub Admin registered for " + assignedService.name()
                        + ". They can login with POST /login/password using their email and password.")
                .timestamp(LocalDateTime.now().toString())
                .data(AdminRegisterResponse.AdminInfo.builder()
                        .id(subAdmin.getId())
                        .email(subAdmin.getEmail())
                        .role(subAdmin.getRole())
                        .adminType(subAdmin.getAdminType().name())
                        .assignedService(subAdmin.getAssignedService().name())
                        .approvalStatus(subAdmin.getApprovalStatus().name())
                        .firstName(firstName)
                        .lastName(lastName)
                        .mobileNumber(mobile)
                        .alternateMobileNumber(altMobile)
                        .build())
                .build();
    }

    private AdminRegisterResponse failure(String message) {
        return AdminRegisterResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    private String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000; // 6-digit numeric OTP
        return String.valueOf(otp);
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {

        Optional<Admin> adminOptional = adminRepository.findByEmail(request.getEmail());

        if (adminOptional.isEmpty()) {
            return buildFailureResponse("Admin with this email does not exist");
        }

        Admin admin = adminOptional.get();

        // Check if already verified
        if (admin.isVerified()) {
            return buildFailureResponse("Email is already verified");
        }

        // Check if OTP is blocked
        if (admin.isOtpBlocked()) {
            return buildFailureResponse("OTP is blocked due to multiple wrong attempts. Request a new OTP.");
        }

        // Check OTP expiry
        if (admin.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return buildFailureResponse("OTP has expired");
        }

        // Check OTP match
        if (!admin.getOtp().equals(request.getOtp())) {
            // Decrease attempts
            int remaining = admin.getOtpAttempts() - 1;
            admin.setOtpAttempts(remaining);

            // Block OTP if attempts exhausted
            if (remaining <= 0) {
                admin.setOtpBlocked(true);
            }

            adminRepository.save(admin);

            return VerifyOtpResponse.builder()
                    .success(false)
                    .message("Invalid OTP. Remaining attempts: " + remaining)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                    .build();
        }

        // ✅ OTP correct → reset attempts and unblock
        admin.setVerified(true);
        admin.setOtp(request.getOtp());
        admin.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        admin.setOtpAttempts(3); // reset for next OTP
        admin.setOtpBlocked(false);

        adminRepository.save(admin);

        auditService.logAction(admin.getId(), "EMAIL_VERIFIED");

        // ✅ Encrypt fields for the response
        return VerifyOtpResponse.builder()
                .success(true)
                .message("Email verified successfully")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .data(VerifyOtpResponse.AdminInfo.builder()
                        .id(admin.getId())
                        .email(encryptionService.encrypt(admin.getEmail()))
                        .role(encryptionService.encrypt(admin.getRole()))
                        .firstName(encryptionService.encrypt(admin.getFirstName()))
                        .lastName(encryptionService.encrypt(admin.getLastName()))
                        .mobileNumber(encryptionService.encrypt(admin.getMobileNumber()))
                        .alternateMobileNumber(encryptionService.encrypt(admin.getAlternateMobileNumber()))
                        .build())
                .build();
    }

    // helper to reduce repetition
    private VerifyOtpResponse buildFailureResponse(String message) {
        return VerifyOtpResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .build();
    }

    public LoginOtpResponse requestLoginOtp(String email) {
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);
        if (adminOptional.isEmpty()) {
            return LoginOtpResponse.builder()
                    .success(false)
                    .message("Admin with this email does not exist")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        Admin admin = adminOptional.get();

        if (!admin.isVerified()) {
            return LoginOtpResponse.builder()
                    .success(false)
                    .message("Email is not verified. Complete registration first.")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Cooldown check (30 seconds)
        if (admin.getLastOtpSentAt() != null &&
                admin.getLastOtpSentAt().plusSeconds(30).isAfter(LocalDateTime.now())) {
            return LoginOtpResponse.builder()
                    .success(false)
                    .message("Please wait before requesting a new OTP.")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Generate OTP
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        admin.setOtp(otp);
        admin.setOtpExpiry(expiry);
        admin.setLastOtpSentAt(LocalDateTime.now());
        admin.setOtpAttempts(3);
        admin.setOtpBlocked(false);
        adminRepository.save(admin);

        emailService.sendOtp(admin.getEmail(), otp);

        return LoginOtpResponse.builder()
                .success(true)
                .message("Login OTP sent successfully to registered email.")
                .data(LoginOtpResponse.OtpData.builder()
                        .validForMinutes(5)
                        .otpType("login")
                        .email(encryptionService.encrypt(admin.getEmail()))
                        .expiresAt(expiry.toString())
                        .cooldownSeconds(30)
                        .build())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public LoginOtpVerifyResponse verifyLoginOtp(LoginOtpVerifyRequest request) {
        Optional<Admin> adminOptional = adminRepository.findByEmail(request.getEmail());

        if (adminOptional.isEmpty()) {
            return LoginOtpVerifyResponse.builder()
                    .success(false)
                    .message("Admin with this email does not exist")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        Admin admin = adminOptional.get();

        // Email verification check
        if (!admin.isVerified()) {
            return LoginOtpVerifyResponse.builder()
                    .success(false)
                    .message("Email is not verified. Complete registration first.")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Check if OTP is blocked due to previous failed attempts
        if (admin.isOtpBlocked()) {
            return LoginOtpVerifyResponse.builder()
                    .success(false)
                    .message("OTP is blocked due to multiple wrong attempts. Request a new OTP.")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Check OTP expiry
        if (admin.getOtpExpiry() == null || admin.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return LoginOtpVerifyResponse.builder()
                    .success(false)
                    .message("OTP has expired. Request a new one.")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Check OTP match
        if (!admin.getOtp().equals(request.getOtp())) {
            int remainingAttempts = admin.getOtpAttempts() - 1;
            admin.setOtpAttempts(remainingAttempts);

            if (remainingAttempts <= 0) {
                admin.setOtpBlocked(true); // block further OTP attempts
            }

            adminRepository.save(admin);

            return LoginOtpVerifyResponse.builder()
                    .success(false)
                    .message("Invalid OTP. Remaining attempts: " + remainingAttempts)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // ✅ Correct OTP → reset attempts and unblock
        admin.setOtp(request.getOtp());
        admin.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        admin.setOtpAttempts(3); // reset for next login
        admin.setOtpBlocked(false);
        adminRepository.save(admin);

        // Admin login should now go through User Service
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Admin login should be performed through User Service at /api/v1/auth/login");
    }

    public ResendOtpResponse resendOtp(String email) {
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);
        if (adminOptional.isEmpty()) {
            return ResendOtpResponse.builder()
                    .success(false)
                    .message("Admin with this email does not exist")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        Admin admin = adminOptional.get();

        // ✅ For registration OTP, block if already verified
        if (!admin.isVerified() && admin.getOtp() == null) {
            return ResendOtpResponse.builder()
                    .success(false)
                    .message("Email is already verified or registration not pending OTP")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Calculate cooldown based on resend attempt count
        int attemptsLeft = admin.getOtpAttempts(); // max 3
        long secondsSinceLastOtp = admin.getLastOtpSentAt() != null
                ? java.time.Duration.between(admin.getLastOtpSentAt(), LocalDateTime.now()).getSeconds()
                : Long.MAX_VALUE;

        long requiredCooldown;
        switch (3 - attemptsLeft + 1) {
            case 1 -> requiredCooldown = 30;      // 1st resend → 30 sec
            case 2 -> requiredCooldown = 120;     // 2nd resend → 2 min
            case 3 -> requiredCooldown = 3600;    // 3rd resend → 1 hr
            default -> requiredCooldown = 0;
        }

        if (secondsSinceLastOtp < requiredCooldown) {
            return ResendOtpResponse.builder()
                    .success(false)
                    .message("Please wait " + (requiredCooldown - secondsSinceLastOtp) + " seconds before requesting a new OTP.")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        if (attemptsLeft <= 0) {
            return ResendOtpResponse.builder()
                    .success(false)
                    .message("Maximum OTP attempts reached. Please contact support.")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Generate new OTP
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        admin.setOtp(otp);
        admin.setOtpExpiry(expiry);
        admin.setLastOtpSentAt(LocalDateTime.now());
        admin.setOtpAttempts(attemptsLeft - 1); // decrease remaining attempts
        admin.setOtpBlocked(false); // unblock OTP if it was blocked
        adminRepository.save(admin);

        emailService.sendOtp(admin.getEmail(), otp);

        return ResendOtpResponse.builder()
                .success(true)
                .message("OTP resent successfully")
                .data(ResendOtpResponse.OtpData.builder()
                        .validForMinutes(5)
                        .otpType("resend")
                        .email(encryptionService.encrypt(admin.getEmail()))
                        .expiresAt(expiry.toString())
                        .attemptsLeft(admin.getOtpAttempts())
                        .cooldownSeconds(requiredCooldown)
                        .build())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public ForgotPasswordResponse sendResetOtp(String email) {
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);

        if (adminOptional.isEmpty()) {
            return ForgotPasswordResponse.builder()
                    .success(false)
                    .message("Admin with this email does not exist")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        Admin admin = adminOptional.get();

        // Cooldown: 30 seconds before resending
        long secondsSinceLastOtp = admin.getLastOtpSentAt() != null
                ? Duration.between(admin.getLastOtpSentAt(), LocalDateTime.now()).getSeconds()
                : Long.MAX_VALUE;

        if (secondsSinceLastOtp < 30) {
            return ForgotPasswordResponse.builder()
                    .success(false)
                    .message("Please wait " + (30 - secondsSinceLastOtp) + " seconds before requesting a new OTP.")
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Generate 6-digit OTP
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        admin.setOtp(otp);
        admin.setOtpExpiry(expiry);
        admin.setLastOtpSentAt(LocalDateTime.now());
        admin.setOtpAttempts(3); // reset attempts for reset
        admin.setOtpBlocked(false);
        adminRepository.save(admin);

        // Send OTP via email
        emailService.sendOtp(admin.getEmail(), otp);

        return ForgotPasswordResponse.builder()
                .success(true)
                .message("OTP sent to email. Use it within 5 minutes")
                .data(ForgotPasswordResponse.ForgotPasswordData.builder()
                        .email(encryptionService.encrypt(admin.getEmail()))
                        .otpType("password_reset")
                        .validForMinutes(5)
                        .expiresAt(expiry.toString())
                        .cooldownSeconds(30)
                        .build())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public PasswordResetResponse resetPassword(PasswordResetRequest request) {

        Optional<Admin> adminOptional = adminRepository.findByEmail(request.getEmail());

        if (adminOptional.isEmpty()) {
            return PasswordResetResponse.builder()
                    .success(false)
                    .message("Admin with this email does not exist")
                    .data(null)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        Admin admin = adminOptional.get();

        if (!admin.isVerified()) {
            return PasswordResetResponse.builder()
                    .success(false)
                    .message("Email is not verified. Complete registration first.")
                    .data(null)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        if (admin.getOtp() == null || admin.getOtpExpiry() == null || admin.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return PasswordResetResponse.builder()
                    .success(false)
                    .message("OTP has expired. Request a new one.")
                    .data(null)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        if (!admin.getOtp().equals(request.getOtp())) {
            int remaining = admin.getOtpAttempts() - 1;
            admin.setOtpAttempts(remaining);

            if (remaining <= 0) {
                admin.setOtpBlocked(true);
            }

            adminRepository.save(admin);

            return PasswordResetResponse.builder()
                    .success(false)
                    .message("Invalid OTP. Remaining attempts: " + remaining)
                    .data(null)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return PasswordResetResponse.builder()
                    .success(false)
                    .message("Passwords do not match")
                    .data(null)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // Password strength check
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!request.getNewPassword().matches(passwordRegex)) {
            return PasswordResetResponse.builder()
                    .success(false)
                    .message("Password must be at least 8 characters, include uppercase, lowercase, number, and special character")
                    .data(null)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        // ✅ All checks passed → reset password
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.setOtp(request.getOtp());
        admin.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        admin.setOtpAttempts(3); // reset attempts
        admin.setOtpBlocked(false);
        admin.setLastOtpSentAt(LocalDateTime.now()); // ✅ add this
        adminRepository.save(admin);

        auditService.logAction(admin.getId(), "PASSWORD_RESET");

        return PasswordResetResponse.builder()
                .success(true)
                .message("Password reset successfully")
                .data(PasswordResetResponse.AdminInfo.builder()
                        .id(admin.getId())
                        .email(admin.getEmail())
                        .firstName(encryptionService.encrypt(admin.getFirstName()))
                        .lastName(encryptionService.encrypt(admin.getLastName()))
                        .role(admin.getRole())
                        .build())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    private String decryptSafely(String value, String defaultValue) {
        if (value == null) return defaultValue;
        try {
            return encryptionService.decrypt(value);
        } catch (Exception e) {
            return value;
        }
    }
}
