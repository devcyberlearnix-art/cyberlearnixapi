package com.user.register.service;



import java.io.InputStream;

import java.time.Duration;

import java.time.LocalDateTime;

import java.util.HashMap;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;

import java.util.Random;

import java.util.UUID;

import com.user.register.entity.UserSession;



import com.user.register.util.BearerTokenResolver;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.server.ResponseStatusException;

import com.cloudinary.Cloudinary;

import com.cloudinary.utils.ObjectUtils;

import java.io.ByteArrayOutputStream;



import com.user.register.dto.ApiResponse;

import com.user.register.dto.ForgotPasswordResponseData;

import com.user.register.dto.LoginRequest;

import com.user.register.dto.LoginResponse;

import com.user.register.entity.AuditLog;

import com.user.register.entity.User;

import com.user.register.exception.LoginFailedException;

import com.user.register.repository.AuditLogRepository;

import com.user.register.repository.UserRepository;

import com.user.register.repository.UserSessionRepository;

import com.user.register.security.JwtUtil;

import com.user.register.util.CountryCodes;

import com.user.register.util.SecurityUtils;



import jakarta.mail.internet.MimeMessage;

import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;



@Service

@RequiredArgsConstructor

@Transactional

@Slf4j

public class RegistrationService {

    private final JwtUtil jwtUtil;

    private final UserSessionRepository userSessionRepository;   // ✅ inject

    private final AuditLogRepository auditLogRepository;         // ✅ inject

    private final UserRepository userRepository;

    private final OtpService otpService;

    private final JavaMailSender mailSender;

    private final BCryptPasswordEncoder passwordEncoder; // inject bean

    private static final int MAX_FAILED_LOGIN = 5;

    private final TokenBlacklistService blacklistService;

    private final Cloudinary cloudinary;

    private final org.springframework.web.client.RestTemplate restTemplate;

    

    @Value("${admin.service.url:http://localhost:8087}")

    private String adminServiceUrl;



    @Value("${cloudinary.folder:cyberlearnix}")

    private String folder;



    @Value("${app.encryption.key:1234567890123456}")

    private String encryptionKey;

    @Value("${app.otp.log-value:true}")
    private boolean logOtpValue;

    private String confirmPassword;



    // ✅ ADD HERE (inside class)

    private String generateResetToken() {

        return UUID.randomUUID().toString().replace("-", "") +

                UUID.randomUUID().toString().replace("-", "");

    }



    @Value("${spring.mail.username}")

    private String fromEmail;



    private static final int MAX_OTP_ATTEMPTS = 5;

    private byte[] secretKey;



    public User register(User user, HttpServletRequest request) throws Exception {



        // ================= GET CLIENT IP =================

        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {

            ipAddress = request.getRemoteAddr();

        }



        // ================= DEVICE + BROWSER + OS =================

        String userAgent = request.getHeader("User-Agent");

        String device = detectDevice(userAgent);

        String browser = detectBrowser(userAgent);

        String os = detectOS(userAgent);

        if (device == null || device.isEmpty()) device = "Unknown Device";



        // ================= PASSWORD VALIDATION =================

        if (user.getPassword() == null || user.getPassword().isEmpty())

            throw new RuntimeException("Password is required");

        if (user.getConfirmPassword() == null || user.getConfirmPassword().isEmpty())

            throw new RuntimeException("Confirm Password is required");

        if (!user.getPassword().equals(user.getConfirmPassword()))

            throw new RuntimeException("Password and Confirm Password do not match");

        if (!isPasswordStrong(user.getPassword()))

            throw new RuntimeException("Password is too weak");



        // ================= COUNTRY CODE VALIDATION =================

        String countryCode = user.getCountryCode();

        if (countryCode == null || !CountryCodes.VALID_CODES.contains(countryCode)) {

            throw new RuntimeException("Invalid country code");

        }



        // ================= MOBILE VALIDATION =================

        if (user.getMobile() == null || !user.getMobile().matches("\\d{6,12}")) {

            throw new RuntimeException("Mobile number must be 6-12 digits");

        }



        // ================= FULL MOBILE =================

        System.out.println("=== FULL MOBILE CONSTRUCTION ===");

        System.out.println("CountryCode: " + countryCode);

        System.out.println("Mobile: " + user.getMobile());

        String fullMobile = countryCode + user.getMobile();

        System.out.println("FullMobile: " + fullMobile);

        System.out.println("FullMobile is null: " + (fullMobile == null));



        // ================= DUPLICATE MOBILE CHECK =================

        String encryptedMobile = SecurityUtils.encrypt(fullMobile, encryptionKey);

        System.out.println("=== DUPLICATE MOBILE CHECK ===");

        System.out.println("Encrypted mobile: " + encryptedMobile);

        Optional<User> existingMobileUser = userRepository.findByMobile(encryptedMobile);

        System.out.println("Existing mobile user found: " + existingMobileUser.isPresent());



        if (existingMobileUser.isPresent()) {

            User mobileUser = existingMobileUser.get();

            boolean sameEmailAsRequest = user.getEmail() != null
                    && mobileUser.getEmail() != null
                    && user.getEmail().equalsIgnoreCase(mobileUser.getEmail());

            if (mobileUser.getStatus() == User.Status.ACTIVE) {

                throw new RuntimeException("Mobile number already registered");

            }

            if (mobileUser.getStatus() == User.Status.PENDING_VERIFICATION) {

                if (!sameEmailAsRequest) {
                    throw new RuntimeException("Mobile number already registered");
                }

                String otp = generateOTP();

                otpService.createSession(mobileUser.getEmail(), "registration", otp, 5, 5);

                // Send OTP email - don't fail registration if email fails
                try {
                    sendOtpEmail(mobileUser.getEmail(), otp, "Registration OTP");
                } catch (Exception e) {
                    log.error("Failed to send registration OTP email to: {}", mobileUser.getEmail(), e);
                }

                return mobileUser;

            }

        }



        // ================= DUPLICATE EMAIL CHECK =================

        System.out.println("=== DUPLICATE EMAIL CHECK ===");

        System.out.println("Email: " + user.getEmail());

        Optional<User> existingUserOpt = userRepository.findByEmail(user.getEmail());

        System.out.println("Existing email user found: " + existingUserOpt.isPresent());

        if (existingUserOpt.isPresent()) {

            User existingUser = existingUserOpt.get();

            if (existingUser.getStatus() == User.Status.ACTIVE)

                throw new RuntimeException("Email already registered");

            if (existingUser.getStatus() == User.Status.PENDING_VERIFICATION) {

                if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) {

                    existingUser.setProfilePhoto(user.getProfilePhoto());

                }

                userRepository.save(existingUser);

                String otp = generateOTP();

                otpService.createSession(existingUser.getEmail(), "registration", otp, 5, 5);

                // Send OTP email - don't fail registration if email fails
                try {
                    sendOtpEmail(existingUser.getEmail(), otp, "Registration OTP");
                } catch (Exception e) {
                    log.error("Failed to send registration OTP email to: {}", existingUser.getEmail(), e);
                }

                return existingUser;

            }

        }



        // ================= NEW USER =================

        user.setFirstName(SecurityUtils.encrypt(user.getFirstName(), encryptionKey));

        user.setLastName(SecurityUtils.encrypt(user.getLastName(), encryptionKey));

        user.setMobile(encryptedMobile); // Save encrypted mobile

        // user.setMobileHash(SecurityUtils.hash(fullMobile)); // Temporarily disabled

        if (user.getDob() != null)

            user.setDob(SecurityUtils.encrypt(user.getDob(), encryptionKey));

        if (user.getCity() != null)

            user.setCity(SecurityUtils.encrypt(user.getCity(), encryptionKey));

        if (user.getState() != null)

            user.setState(SecurityUtils.encrypt(user.getState(), encryptionKey));

        if (user.getCountry() != null)

            user.setCountry(SecurityUtils.encrypt(user.getCountry(), encryptionKey));

        if (user.getOrganization() != null)

            user.setOrganization(SecurityUtils.encrypt(user.getOrganization(), encryptionKey));

        if (user.getProfilePhoto() != null)

            user.setProfilePhoto(SecurityUtils.encrypt(user.getProfilePhoto(), encryptionKey));

        if (user.getPreferredLanguage() != null)

            user.setPreferredLanguage(SecurityUtils.encrypt(user.getPreferredLanguage(), encryptionKey));

        if (user.getSkills() != null)

            user.setSkills(SecurityUtils.encrypt(user.getSkills(), encryptionKey));

        if (user.getFieldOfStudy() != null)

            user.setFieldOfStudy(SecurityUtils.encrypt(user.getFieldOfStudy(), encryptionKey));

        if (user.getHighestQualification() != null)

            user.setHighestQualification(SecurityUtils.encrypt(user.getHighestQualification(), encryptionKey));



        user.setPassword(SecurityUtils.hashPassword(user.getPassword()));

        user.setStatus(User.Status.PENDING_VERIFICATION);

        user.setRole(User.Role.STUDENT);

        user.setIsInstructorApproved(false);



        // ================= SAVE DEVICE INFO =================

        user.setIpAddress(ipAddress);

        user.setDevice(device);

        user.setBrowser(browser);

        user.setOs(os);

        user.setUserAgent(userAgent);



        // ================= SAVE USER =================

        User savedUser = userRepository.save(user);



        // ================= GENERATE OTP =================

        String otp = generateOTP();

        log.info("Registration OTP generated for user: {}", savedUser.getEmail());

        OtpService.OtpSession otpSession = otpService.createSession(savedUser.getEmail(), "registration", otp, 5, 5);

        log.debug("Registration OTP session created with id: {}", otpSession.sessionId());



        // ================= AUDIT LOG =================

        auditLogRepository.save(AuditLog.builder()

                .user(savedUser)

                .action("REGISTER")

                .ipAddress(ipAddress)

                .device(device)

                .createdAt(LocalDateTime.now())

                .build());



        // Send OTP email - don't fail registration if email fails
        try {
            sendOtpEmail(savedUser.getEmail(), otp, "Registration OTP");
        } catch (Exception e) {
            log.error("Failed to send registration OTP email to: {}", savedUser.getEmail(), e);
            // Continue with registration even if email fails
        }

        return savedUser;

    }



    private String detectDevice(String userAgent) {



        if (userAgent == null)

            return "Unknown Device";



        userAgent = userAgent.toLowerCase();



        // Postman

        if (userAgent.contains("postmanruntime") || userAgent.contains("postman"))

            return "Postman";



        // Mobile

        if (userAgent.contains("android"))

            return "Android Mobile";



        if (userAgent.contains("iphone"))

            return "iPhone";



        if (userAgent.contains("ipad"))

            return "iPad";



        // Desktop

        if (userAgent.contains("windows"))

            return "Windows Desktop";



        if (userAgent.contains("mac"))

            return "Mac Desktop";



        if (userAgent.contains("linux"))

            return "Linux Desktop";

        return "Unknown Device";

    }



    private String detectBrowser(String userAgent) {



        if (userAgent == null) return "Unknown Browser";



        userAgent = userAgent.toLowerCase();



        if (userAgent.contains("postman"))

            return "Postman";



        if (userAgent.contains("chrome"))

            return "Chrome";



        if (userAgent.contains("firefox"))

            return "Firefox";



        if (userAgent.contains("safari"))

            return "Safari";



        if (userAgent.contains("edge"))

            return "Edge";



        return "Unknown Browser";

    }



    private String detectOS(String userAgent) {



        if (userAgent == null)

            return "Unknown OS";



        userAgent = userAgent.toLowerCase();



        if (userAgent.contains("postmanruntime") || userAgent.contains("postman"))

            return "Development Environment";



        if (userAgent.contains("android"))

            return "Android";



        if (userAgent.contains("iphone") || userAgent.contains("ios"))

            return "iOS";



        if (userAgent.contains("windows"))

            return "Windows";



        if (userAgent.contains("mac"))

            return "MacOS";



        if (userAgent.contains("linux"))

            return "Linux";



        return "Unknown OS";

    }



    /**

     * Simple password strength check

     */

    private boolean isPasswordStrong(String password) {

        if (password.length() < 8) return false;

        if (!password.matches(".*[A-Z].*")) return false;

        if (!password.matches(".*[a-z].*")) return false;

        if (!password.matches(".*\\d.*")) return false;

        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) return false;

        return true;

    }



    private void sendOtpEmail(String email, String otp, String passwordResetOtp) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);



            helper.setFrom(fromEmail);

            helper.setTo(email);

            helper.setSubject("Registration OTP Verification");



            // Load HTML template

            String htmlContent = loadTemplate(otp);



            helper.setText(htmlContent, true); // ✅ true = HTML



            mailSender.send(message);



            log.info("Registration OTP email sent successfully to: {}", email);
            if (logOtpValue) {
                log.info("Registration OTP value for {} is {}", email, otp);
            }



        } catch (Exception e) {

            log.error("Registration OTP email sending failed for: {}", email, e);

        }

    }



    private String loadTemplate(String otp) {

        try {

            InputStream inputStream = getClass()

                    .getResourceAsStream("/templates/otp-email.html");



            String template = new String(inputStream.readAllBytes());



            return template.replace("{{OTP}}", otp);



        } catch (Exception e) {

            throw new RuntimeException("Failed to load email template", e);

        }

    }



    /**

     * Upload and process profile photo

     * Rules:

     * - Max 5MB

     * - JPG / PNG / WEBP only

     * - Resize to 512x512

     */

    public String uploadProfilePhoto(MultipartFile file) throws Exception {



        // 1️⃣ Check empty

        if (file == null || file.isEmpty()) {

            throw new RuntimeException("File is empty");

        }



        // 2️⃣ Validate size (5MB)

        long maxSize = 5 * 1024 * 1024;

        if (file.getSize() > maxSize) {

            throw new RuntimeException("File size must be less than 5MB");

        }



        // 3️⃣ Validate content type

        String contentType = file.getContentType();

        if (contentType == null ||

                !(contentType.equals("image/jpeg") ||

                        contentType.equals("image/png") ||

                        contentType.equals("image/webp"))) {



            throw new RuntimeException("Only JPG, PNG, WEBP formats allowed");

        }



        // 4️⃣ Read image

        java.awt.image.BufferedImage originalImage =

                javax.imageio.ImageIO.read(file.getInputStream());



        if (originalImage == null) {

            throw new RuntimeException("Invalid image file");

        }



        // 5️⃣ Resize to 512x512

        java.awt.image.BufferedImage resizedImage =

                new java.awt.image.BufferedImage(512, 512, java.awt.image.BufferedImage.TYPE_INT_RGB);



        java.awt.Graphics2D g = resizedImage.createGraphics();

        g.drawImage(originalImage, 0, 0, 512, 512, null);

        g.dispose();



        // 6️⃣ Generate file name

        String extension = contentType.equals("image/png") ? "png"

                : contentType.equals("image/webp") ? "webp"

                : "jpg";



        String fileName = java.util.UUID.randomUUID().toString();



        // 7️⃣ Save resized image to byte array

        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();

        javax.imageio.ImageIO.write(

                resizedImage,

                extension.equals("jpg") ? "jpeg" : extension,

                os

        );

        byte[] fileBytes = os.toByteArray();



        // 8️⃣ Upload to Cloudinary

        Map<?, ?> options = ObjectUtils.asMap(

                "folder", folder,

                "public_id", fileName,

                "resource_type", "image"

        );

        Map<?, ?> uploadResult = cloudinary.uploader().upload(fileBytes, options);

        return (String) uploadResult.get("secure_url");

    }



    public ResponseEntity<Map<String, Object>> verifyOTP(

            String email,

            String otp,

            HttpServletRequest request,

            HttpServletResponse response) {

        // 1️⃣ Find user

        User user = userRepository.findByEmail(email)

                .orElseThrow(() ->

                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));



        // 2️⃣ Account status checks

        if (user.getStatus() == User.Status.LOCKED) {

            return buildOtpResponse(HttpStatus.FORBIDDEN,

                    false,

                    "Account locked due to failed OTP attempts",

                    0,

                    0);

        }



        if (user.getStatus() == User.Status.SUSPENDED) {

            return buildOtpResponse(HttpStatus.FORBIDDEN,

                    false,

                    "Account suspended",

                    0,

                    0);

        }



        // 3️⃣ Verify OTP from Redis session
        long secondsToExpire = otpService.getLatestSessionTtlSeconds(email, "registration");
        if (secondsToExpire <= 0) {
            return buildOtpResponse(
                HttpStatus.GONE,
                false,
                "OTP expired",
                0,
                0
            );
        }

        OtpService.OtpVerifyResult verifyResult = otpService.verifyLatestSession(email, otp, "registration", true);
        if (!verifyResult.valid()) {
            int remainingAttempts = verifyResult.remainingAttempts();
            if (remainingAttempts <= 0 && "Invalid OTP".equals(verifyResult.reason())) {
            user.setStatus(User.Status.LOCKED);
            userRepository.save(user);

            return buildOtpResponse(HttpStatus.FORBIDDEN,
                false,
                "Too many failed OTP attempts. Account locked.",
                0,
                0);
            }

            HttpStatus status = "OTP session expired or not found".equals(verifyResult.reason())
                ? HttpStatus.GONE
                : HttpStatus.UNAUTHORIZED;
            String message = "OTP session expired or not found".equals(verifyResult.reason())
                ? "OTP expired"
                : verifyResult.reason();

            return buildOtpResponse(status,
                false,
                message,
                remainingAttempts,
                secondsToExpire);
        }



        // 7️⃣ OTP correct

        user.setStatus(User.Status.ACTIVE);

        userRepository.save(user);



        // Prevent replay attack by consuming successful Redis OTP session.



        // 8️⃣ Device Detection

        String userAgent = request.getHeader("User-Agent");

        String ipAddress = request.getRemoteAddr();



        String device = detectDevice(userAgent);

        String browser = detectBrowser(userAgent);

        String os = detectOS(userAgent);



        // 9️⃣ Audit logging

        auditLogRepository.save(

                AuditLog.builder()

                        .user(user)

                        .action("VERIFY_OTP_SUCCESS")

                        .ipAddress(ipAddress)

                        .device(device)

                        .createdAt(LocalDateTime.now())

                        .build()

        );



        // 🔟 Generate Tokens

        String accessToken = jwtUtil.generateAccessToken(user.getId().toString(), user.getRole().name());

        String refreshToken = jwtUtil.generateRefreshToken(user, user.getId().toString(), user.getRole().name());

        // Save refresh token for rotation / blacklist

        user.setRefreshToken(refreshToken);

        user.setDevice(device);

        userRepository.save(user);



        // Invalidate old sessions

        List<UserSession> existingSessions = userSessionRepository.findByUser(user);

        for (UserSession s : existingSessions) {

            if (s.getAccessToken() != null) {

                blacklistService.blacklistToken(s.getAccessToken());

            }

            if (s.getRefreshToken() != null) {

                blacklistService.blacklistToken(s.getRefreshToken());

            }

        }

        userSessionRepository.deleteAll(existingSessions);



        // Save new session

        UserSession userSession = UserSession.builder()

                .user(user)

                .accessToken(accessToken)

                .refreshToken(refreshToken)

                .deviceInfo(userAgent)

                .ipAddress(ipAddress)

                .expiresAt(LocalDateTime.now().plusDays(30))

                .build();

        userSessionRepository.save(userSession);

        // 11️⃣ Access Token Cookie

        Cookie accessCookie = new Cookie("accessToken", accessToken);

        accessCookie.setHttpOnly(true);

        accessCookie.setSecure(true);

        accessCookie.setPath("/");

        accessCookie.setMaxAge(15 * 60);

        response.addCookie(accessCookie);



        // 12️⃣ Refresh Token Cookie

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

        refreshCookie.setHttpOnly(true);

        refreshCookie.setSecure(true);

        refreshCookie.setPath("/");

        refreshCookie.setMaxAge(30 * 24 * 60 * 60);

        response.addCookie(refreshCookie);



        // 13️⃣ Response Data

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("id", user.getId());

        data.put("firstName", user.getFirstName());

        data.put("lastName", user.getLastName());

        data.put("email", user.getEmail());

        data.put("mobile", user.getMobile());

        data.put("dob", user.getDob());

        data.put("profilePhoto", user.getProfilePhoto());

        data.put("city", user.getCity());

        data.put("state", user.getState());

        data.put("country", user.getCountry());

        data.put("preferredLanguage", user.getPreferredLanguage());

        data.put("organization", user.getOrganization());

        data.put("skills", user.getSkills());

        data.put("fieldOfStudy", user.getFieldOfStudy());

        data.put("highestQualification", user.getHighestQualification());

        data.put("status", user.getStatus());

        data.put("role", user.getRole());

        data.put("isInstructorApproved", user.getIsInstructorApproved());



        Map<String, Object> responseBody = new LinkedHashMap<>();

        responseBody.put("success", true);

        responseBody.put("message", "OTP verified successfully");

        responseBody.put("data", data);

        responseBody.put("timestamp", LocalDateTime.now());



        return new ResponseEntity<>(responseBody, HttpStatus.OK);

    }



    private ResponseEntity<Map<String, Object>> buildOtpResponse(

            HttpStatus status,

            boolean success,

            String message,

            int remainingAttempts,

            long expiresInSeconds) {



        Map<String, Object> data = new HashMap<>();

        data.put("remainingAttempts", remainingAttempts);

        data.put("expiresInSeconds", expiresInSeconds);



        Map<String, Object> body = new HashMap<>();

        body.put("success", success);

        body.put("message", message);

        body.put("data", data);

        body.put("timestamp", LocalDateTime.now());



        return new ResponseEntity<>(body, status);

    }



    /**

     * Fetch user by email

     */

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)

                .orElseThrow(() -> new RuntimeException("User not found"));

    }



    /**

     * Generate 6-digit OTP

     */

    private String generateOTP() {

        Random random = new Random();

        return String.valueOf(100000 + random.nextInt(900000));

    }



    public ResponseEntity<Map<String, Object>> loginWithPassword(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        LocalDateTime now = LocalDateTime.now();



        // 1️⃣ Fetch user

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        

        if (optionalUser.isEmpty()) {

            // Try admin login

            try {

                Map<String, Object> adminRequest = new HashMap<>();

                adminRequest.put("email", request.getEmail());

                adminRequest.put("password", request.getPassword());

                

                String adminLoginUrl = adminServiceUrl + "/api/v1/admins/login";

                ResponseEntity<Map> adminResponse = restTemplate.postForEntity(adminLoginUrl, adminRequest, Map.class);

                

                if (adminResponse.getStatusCode() == HttpStatus.OK && adminResponse.getBody() != null) {

                    // Return admin response directly without wrapping

                    Map<String, Object> adminBody = adminResponse.getBody();

                    Map<String, Object> responseBody = new HashMap<>();

                    responseBody.putAll(adminBody);

                    responseBody.put("success", true);

                    responseBody.put("message", "Admin login successful");

                    return ResponseEntity.ok(responseBody);

                }

            } catch (Exception e) {

                // Admin login failed, return unauthorized

                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        }



        User user = optionalUser.get();



        // 2️⃣ Account status checks

        if (user.getStatus() == User.Status.PENDING_VERIFICATION)

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email not verified");

        if (user.getStatus() == User.Status.LOCKED)

            throw new ResponseStatusException(HttpStatus.LOCKED, "Account locked due to too many failed login attempts");

        if (user.getStatus() == User.Status.SUSPENDED)

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account suspended by admin");



        // 3️⃣ Instructor approval check

        if (user.getRole() == User.Role.INSTRUCTOR && !user.getIsInstructorApproved())

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Instructor account not approved yet");



        // 4️⃣ Get client IP

        String ipAddress = httpRequest.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress))

            ipAddress = httpRequest.getRemoteAddr();



        // 5️⃣ Detect User-Agent details

        String userAgent = httpRequest.getHeader("User-Agent");

        String deviceType = detectDevice(userAgent);

        String os = detectOS(userAgent);

        String browser = detectBrowser(userAgent);



        if (userAgent != null) {

            String agent = userAgent.toLowerCase();



            // Device detection

            if (agent.contains("postman")) deviceType = "Postman";

            else if (agent.contains("iphone") || agent.contains("android") || agent.contains("mobile"))

                deviceType = "Mobile";

            else if (agent.contains("ipad") || agent.contains("tablet")) deviceType = "Tablet";



            // OS detection

            if (agent.contains("windows")) os = "Windows";

            else if (agent.contains("mac")) os = "MacOS";

            else if (agent.contains("android")) os = "Android";

            else if (agent.contains("iphone") || agent.contains("ios")) os = "iOS";

            else if (agent.contains("linux")) os = "Linux";



            // Browser detection

            if (agent.contains("chrome") && !agent.contains("edge")) browser = "Chrome";

            else if (agent.contains("firefox")) browser = "Firefox";

            else if (agent.contains("safari") && !agent.contains("chrome")) browser = "Safari";

            else if (agent.contains("edge")) browser = "Edge";

        }



        String deviceInfo = deviceType + " - " + os + " - " + browser;



        // 6️⃣ Verify password with brute force lockout

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            int failed = user.getFailedLoginAttempts() == null ? 1 : user.getFailedLoginAttempts() + 1;

            user.setFailedLoginAttempts(failed);



            if (failed >= MAX_FAILED_LOGIN) user.setStatus(User.Status.LOCKED);



            userRepository.save(user);



            auditLogRepository.save(AuditLog.builder()

                    .user(user)

                    .action("LOGIN_FAILED")

                    .ipAddress(ipAddress)

                    .device(deviceInfo)

                    .createdAt(now)

                    .build());



            Map<String, Object> errorData = Map.of(

                    "remainingAttempts", MAX_FAILED_LOGIN - failed,

                    "accountStatus", user.getStatus().name()

            );



            throw new LoginFailedException("Invalid credentials", errorData);

        }



        // 7️⃣ Reset failed attempts on success

        user.setFailedLoginAttempts(0);

        if (user.getCreatedAt() == null) user.setCreatedAt(now);

        user.setUpdatedAt(now);

        user.setLastLoginAt(now);

        userRepository.save(user);



// 8️⃣ Generate strong tokens

        String accessToken = jwtUtil.generateAccessToken(user.getId().toString(), user.getRole().name());

        String refreshToken = jwtUtil.generateRefreshToken(user, user.getId().toString(), user.getRole().name());

        LocalDateTime accessTokenExpiry = now.plusMinutes(15);

        LocalDateTime refreshTokenExpiry = now.plusDays(30);

        // Save refresh token for rotation & blacklist

        user.setRefreshToken(refreshToken);

        user.setDevice(deviceInfo);

        user.setDevice(deviceType);

        user.setBrowser(browser);

        user.setOs(os);

        user.setIpAddress(ipAddress);

        user.setUserAgent(userAgent);

        userRepository.save(user);



        // 8.5️⃣ Create UserSession (required for JwtAuthFilter session validation)

        // Clear any existing sessions for this user first

        java.util.List<UserSession> existingSessions = userSessionRepository.findByUser(user);

        for (UserSession s : existingSessions) {

            if (s.getAccessToken() != null) {

                blacklistService.blacklistToken(s.getAccessToken());

            }

            if (s.getRefreshToken() != null) {

                blacklistService.blacklistToken(s.getRefreshToken());

            }

        }

        userSessionRepository.deleteAll(existingSessions);



        UserSession newSession = UserSession.builder()

                .user(user)

                .accessToken(accessToken)

                .refreshToken(refreshToken)

                .deviceInfo(deviceInfo)

                .ipAddress(ipAddress)

                .expiresAt(now.plusDays(30))

                .build();

        userSessionRepository.save(newSession);



        // 9️⃣ Audit success

        auditLogRepository.save(AuditLog.builder()

                .user(user)

                .action("LOGIN_SUCCESS")

                .ipAddress(ipAddress)

                .device(deviceInfo)

                .createdAt(now)

                .build());



        // 🔟 Set HttpOnly cookies for tokens (CSRF & XSS safe)

        Cookie accessCookie = new Cookie("accessToken", accessToken);

        accessCookie.setHttpOnly(true);

        accessCookie.setPath("/");

        accessCookie.setMaxAge(15 * 60);

        httpResponse.addCookie(accessCookie);



        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

        refreshCookie.setHttpOnly(true);

        refreshCookie.setPath("/");

        refreshCookie.setMaxAge(30 * 24 * 60 * 60);

        httpResponse.addCookie(refreshCookie);



        // 1️⃣1️⃣ Build response

// 1️⃣1️⃣ Build response

        Map<String, Object> responseBody = new LinkedHashMap<>();

        responseBody.put("success", true);

        responseBody.put("statusCode", HttpStatus.OK.value());

        responseBody.put("message", "Login successful");



// ✅ User info

        responseBody.put("userId", user.getId());

        responseBody.put("email", user.getEmail());

        responseBody.put("firstName", user.getFirstName());

        responseBody.put("lastName", user.getLastName());

        responseBody.put("mobile", user.getMobile());



// ✅ Device & system info



        responseBody.put("loginDevice", deviceInfo); // Full string: Device - OS - Browser

        responseBody.put("device", deviceType);      // Just device

        responseBody.put("browser", browser);

        responseBody.put("os", os);

        responseBody.put("userAgent", userAgent);

        responseBody.put("loginIp", ipAddress);



// ✅ Tokens

        responseBody.put("accessToken", accessToken);

        responseBody.put("refreshToken", refreshToken);

        responseBody.put("accessTokenExpiresAt", accessTokenExpiry);

        responseBody.put("refreshTokenExpiresAt", refreshTokenExpiry);



// ✅ Timestamp

        responseBody.put("timestamp", now);



        return new ResponseEntity<>(responseBody, HttpStatus.OK);

    }



    /**

     * Request OTP for login

     * Used by POST /auth/login/otp/request

     */

    public ApiResponse<Map<String, Object>> sendLoginOtp(String email) {



        Optional<User> userOptional = userRepository.findByEmail(email);



        if (userOptional.isEmpty()) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        }



        User user = userOptional.get();



        if (user.getStatus() != User.Status.ACTIVE) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not active");

        }



        long cooldown = otpService.getCooldownSeconds(email, "login");
        if (cooldown > 0) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "OTP requested too frequently. Please wait 30 seconds.");
        }



        String otp = generateOTP();
        OtpService.OtpSession otpSession = otpService.createSession(email, "login", otp, 5, 5);
        otpService.markCooldown(email, "login", 30);



        sendOtpEmail(user.getEmail(), otp, "Login OTP");



        Map<String, Object> data = new HashMap<>();

        data.put("email", user.getEmail());

        data.put("otpType", "login");

        data.put("otpSessionId", otpSession.sessionId());

        data.put("expiresAt", otpSession.expiresAt());

        data.put("validForMinutes", 5);

        data.put("cooldownSeconds", 30);



        return new ApiResponse<>(

                true,

                "Login OTP sent successfully to registered email.",

                data,

                LocalDateTime.now()

        );

    }



    public ApiResponse<?> verifyLoginOtp(String email, String otp) {

        User user = userRepository.findByEmail(email).orElse(null);



        if (user == null) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        }



        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {

            return buildOtpErrorResponse(

                    "Account temporarily locked until " + user.getLockedUntil(),

                    0,

                    0

            );

        }



        if (user.getStatus() != User.Status.ACTIVE) {

            return buildOtpErrorResponse("Account not active", 0, 0);

        }



        long expiresInSeconds = otpService.getLatestSessionTtlSeconds(email, "login");
        if (expiresInSeconds <= 0) {
            return buildOtpErrorResponse("OTP expired", 0, 0);
        }

        OtpService.OtpVerifyResult verifyResult = otpService.verifyLatestSession(email, otp, "login", true);
        if (!verifyResult.valid()) {
            int remainingAttempts = verifyResult.remainingAttempts();

            if (remainingAttempts <= 0 && "Invalid OTP".equals(verifyResult.reason())) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                return buildOtpErrorResponse(
                        "Maximum OTP attempts reached. Account locked until " + user.getLockedUntil(),
                        0,
                        0
                );
            }

            String message = "OTP session expired or not found".equals(verifyResult.reason())
                    ? "OTP expired"
                    : verifyResult.reason();
            return buildOtpErrorResponse(message, remainingAttempts, expiresInSeconds);
        }



        // Generate tokens

        String accessToken = jwtUtil.generateAccessToken(String.valueOf(user.getId()), user.getRole().name());

        String refreshToken = jwtUtil.generateRefreshToken(user, String.valueOf(user.getId()), user.getRole().name());



        // Invalidate old sessions (enforce single device)

        List<UserSession> existingSessions = userSessionRepository.findByUser(user);

        for (UserSession s : existingSessions) {

            if (s.getAccessToken() != null) {

                blacklistService.blacklistToken(s.getAccessToken());

            }

            if (s.getRefreshToken() != null) {

                blacklistService.blacklistToken(s.getRefreshToken());

            }

        }

        userSessionRepository.deleteAll(existingSessions);



        // Save new session

        HttpServletRequest request = null;

        try {

            request = ((org.springframework.web.context.request.ServletRequestAttributes) 

                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();

        } catch (Exception e) {

            // Fallback if not in web request context

        }

        String deviceInfo = request != null ? request.getHeader("User-Agent") : "Unknown Device";

        if (deviceInfo == null) deviceInfo = "Unknown Device";

        String ipAddress = request != null ? request.getRemoteAddr() : "127.0.0.1";



        UserSession userSession = UserSession.builder()

                .user(user)

                .accessToken(accessToken)

                .refreshToken(refreshToken)

                .deviceInfo(deviceInfo)

                .ipAddress(ipAddress)

                .expiresAt(LocalDateTime.now().plusDays(30))

                .build();

        userSessionRepository.save(userSession);



        Map<String, Object> data = new HashMap<>();

        data.put("accessToken", accessToken);

        data.put("refreshToken", refreshToken);

        data.put("accessTokenExpiresInSeconds", 900);

        data.put("refreshTokenExpiresInDays", 30);

        data.put("sessionId", String.valueOf(userSession.getId()));



        return new ApiResponse<>(

                true,

                "OTP verified successfully",

                data,

                LocalDateTime.now()

        );

    }



    private ApiResponse<Map<String, Object>> buildOtpErrorResponse(String message, int remainingAttempts, long expiresInSeconds) {

        Map<String, Object> data = new HashMap<>();

        data.put("remainingAttempts", remainingAttempts);

        data.put("expiresInSeconds", expiresInSeconds);



        return new ApiResponse<>(

                false,

                message,

                data,

                LocalDateTime.now()

        );

    }



    public LoginResponse refreshAccessToken(String refreshToken) {



        // 1️⃣ Validate refresh token

        if (!jwtUtil.validateToken(refreshToken)) {

            throw new RuntimeException("Invalid or expired refresh token");

        }



        // 2️⃣ Check token type

        String tokenType = jwtUtil.extractTokenType(refreshToken);

        if (!"refresh".equals(tokenType)) {

            throw new RuntimeException("Invalid token type");

        }



        // 3️⃣ Extract userId

        String userId = jwtUtil.extractUserId(refreshToken);



        User user = userRepository.findById(UUID.fromString(userId))

                .orElseThrow(() -> new RuntimeException("User not found"));



        if (user.getStatus() != User.Status.ACTIVE) {

            throw new RuntimeException("User account not active");

        }



        // 4️⃣ Generate new access token (15 minutes)

        String newAccessToken = jwtUtil.generateAccessToken(userId, user.getRole().name());



        // Update session with new access token

        userSessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {

            session.setAccessToken(newAccessToken);

            userSessionRepository.save(session);

        });



        LoginResponse response = new LoginResponse();



        response.setAccessToken(newAccessToken);

        response.setExpiresInSeconds(900);

        response.setUserId(user.getId());

        response.setEmail(user.getEmail());

        response.setAccessToken(newAccessToken);

        response.setRefreshToken(refreshToken); // reuse same refresh token

        response.setExpiresInSeconds(15 * 60); // 900 seconds



        return response;

    }



    // ===================== FORGOT PASSWORD =====================

    public ForgotPasswordResponseData sendResetOtp(String email) {

        User user = userRepository.findByEmail(email)

                .orElseThrow(() -> new RuntimeException("User not found"));



        if (user.getStatus() != User.Status.ACTIVE) {

            throw new RuntimeException("Account not active");

        }



        long cooldown = otpService.getCooldownSeconds(email, "password_reset");
        if (cooldown > 0) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "OTP requested too frequently. Please wait " + cooldown + " seconds.");
        }

        // Generate OTP
        String otp = generateOTP();
        OtpService.OtpSession otpSession = otpService.createSession(email, "password_reset", otp, 10, 5);
        otpService.markCooldown(email, "password_reset", 30);



        // Send OTP email

        sendOtpEmail(user.getEmail(), otp, "Password Reset OTP");



        return new ForgotPasswordResponseData(

            user.getEmail(),

            6, // OTP length

            otpSession.sessionId(),

            LocalDateTime.now(),

            otpSession.expiresAt(),

            30,

            "OTP sent to email. Use it within 10 minutes."

        );

    }



    public Map<String, Object> verifyOtpAndGenerateResetToken(String email, String otp) {



        User user = userRepository.findByEmail(email)

                .orElseThrow(() -> new RuntimeException("User not found"));



        OtpService.OtpVerifyResult verifyResult = otpService.verifyLatestSession(email, otp, "password_reset", true);
        if (!verifyResult.valid()) {
            if ("OTP session expired or not found".equals(verifyResult.reason())) {
                throw new RuntimeException("OTP expired");
            }
            throw new RuntimeException(verifyResult.reason());
        }



        // ✅ Generate secure token

        String resetToken = UUID.randomUUID().toString();



        // ✅ Save token

        user.setResetToken(resetToken);

        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);



        // OTP is consumed by Redis verify call.



        return Map.of(

                "resetToken", resetToken,

                "expiresInMinutes", 15

        );

    }



    public Map<String, Object> resetPasswordWithToken(

            String authHeader,

            String newPassword,

            String confirmPassword

    ) {



        // 🔥 Extract token from "Bearer xxx"

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Missing or invalid Authorization header");

        }



        String token = authHeader.replace("Bearer ", "").trim();



        // ✅ Password match

        if (!newPassword.equals(confirmPassword)) {

            throw new RuntimeException("Passwords do not match");

        }



        // ✅ Password strength

        if (!newPassword.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {

            throw new RuntimeException(

                    "Password must contain uppercase, lowercase, number, special character and be at least 8 characters long"

            );

        }



        // 🔍 Find user by token

        User user = userRepository.findAll().stream()

                .filter(u -> token.equals(u.getResetToken()))

                .findFirst()

                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        // ⏰ expiry check

        if (user.getResetTokenExpiry() == null ||

                user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {

            throw new RuntimeException("Token expired");

        }



        // 🔐 update password

        user.setPassword(passwordEncoder.encode(newPassword));



        // 🧹 clear token

        user.setResetToken(null);

        user.setResetTokenExpiry(null);



        userRepository.save(user);



        return Map.of(

                "email", user.getEmail(),

                "message", "Password reset successfully"

        );

    }



    public Map<String, Object> logoutCurrentDevice(String accessToken, String ipAddress, String deviceInfo) {

        // 1️⃣ Validate token & extract userId

        String userId = jwtUtil.validateAccessTokenAndGetUserId(accessToken);

        User user = userRepository.findById(UUID.fromString(userId))

                .orElseThrow(() -> new RuntimeException("User not found"));



        // 2️⃣ Delete the session from DB instead of in-memory blacklist

        userSessionRepository.findByToken(accessToken).ifPresent(userSessionRepository::delete);



        // 3️⃣ Save audit log

        auditLogRepository.save(AuditLog.builder()

                .user(user)

                .action("LOGOUT")

                .ipAddress(ipAddress)

                .device(deviceInfo)

                .createdAt(LocalDateTime.now())

                .build());



        // 4️⃣ Prepare response

        Map<String, Object> data = new HashMap<>();

        data.put("email", user.getEmail());

        data.put("logoutDevice", deviceInfo);

        data.put("logoutIp", ipAddress);

        data.put("logoutTime", LocalDateTime.now());

        data.put("sessionTerminated", true);



        return data;

    }



    @Transactional

    public Map<String, Object> switchRole(String switchRole, HttpServletRequest httpRequest) {



        // 1️⃣ Extract token

        String token = BearerTokenResolver.resolveToken(httpRequest);



        if (token == null || token.isBlank()) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");

        }



        // 2️⃣ Extract userId + role from JWT

        UUID userId = UUID.fromString(

                jwtUtil.validateAccessTokenAndGetUserId(token)

        );



        String tokenRole = jwtUtil.extractRole(token); // 🔥 IMPORTANT



        // 3️⃣ Convert requested role

        User.Role targetRole = mapSwitchRole(switchRole);



        // 4️⃣ Fetch user from DB

        User user = userRepository.findById(userId).orElse(null);



        if (user == null) {

            if (isAdminRole(tokenRole)) {

                // Admin switching role, but not in DB.

                // Just generate the tokens.

                String jwtRole = toJwtRole(targetRole);

                String accessToken = jwtUtil.generateAccessToken(userId.toString(), jwtRole);

                String refreshToken = jwtUtil.generateRefreshToken(null, userId.toString(), jwtRole);



                Map<String, Object> data = new LinkedHashMap<>();

                data.put("userId", userId);

                data.put("email", "admin@domain.com"); // Email not strictly needed here

                data.put("previousRole", tokenRole);

                data.put("activeRole", jwtRole);

                data.put("accessToken", accessToken);

                data.put("refreshToken", refreshToken);



                return data;

            } else {

                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

            }

        }



        User.Role currentRole = user.getRole();



        // 5️⃣ Validate switch

        validateRoleSwitch(currentRole, targetRole, user, tokenRole);



        // 6️⃣ Update role (Hibernate will auto-save)

        user.setRole(targetRole);

        user.setUpdatedAt(LocalDateTime.now());



        // 7️⃣ Generate NEW tokens with updated role

        String jwtRole = toJwtRole(targetRole);



        String accessToken = jwtUtil.generateAccessToken(

                String.valueOf(user.getId()),

                jwtRole

        );



        String refreshToken = jwtUtil.generateRefreshToken(

                user,

                String.valueOf(user.getId()),

                jwtRole

        );



        // 8️⃣ Response

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("userId", user.getId());

        data.put("email", user.getEmail());

        data.put("previousRole", toJwtRole(currentRole));

        data.put("activeRole", jwtRole);

        data.put("accessToken", accessToken);

        data.put("refreshToken", refreshToken);



        return data;

    }



    private User.Role mapSwitchRole(String role) {



        if (role == null || role.isBlank()) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "switchRole is required");

        }



        String normalized = role.trim().toUpperCase();


        if ("USER".equals(normalized)) {
            return User.Role.STUDENT;
        }

        // Handle ADMIN types - map to MAIN_ADMIN as default or specific admin types
        if (normalized.contains("MAIN_ADMIN")) {
            return User.Role.MAIN_ADMIN;
        }
        if (normalized.contains("SUB_ADMIN")) {
            return User.Role.SUB_ADMIN;
        }

        try {
            return User.Role.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid switchRole. Use USER, INSTRUCTOR, MAIN_ADMIN, SUB_ADMIN");

        }

    }



    private void validateRoleSwitch(User.Role currentRole,

                                    User.Role targetRole,

                                    User user,

                                    String tokenRole) {



        // ❌ Same role

        if (currentRole == targetRole) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,

                    "Already active in role: " + toJwtRole(targetRole));

        }



        // ❌ Nobody can switch TO MAIN_ADMIN or SUB_ADMIN
        if (targetRole == User.Role.MAIN_ADMIN || targetRole == User.Role.SUB_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cannot switch to ADMIN role");
        }



        // ✅ ADMIN (from JWT, not DB)

        if (isAdminRole(tokenRole)) {

            if (targetRole == User.Role.STUDENT || targetRole == User.Role.INSTRUCTOR) {

                return;

            }

        }



        // ✅ USER → INSTRUCTOR

        if (currentRole == User.Role.STUDENT) {

            if (targetRole == User.Role.INSTRUCTOR) {



                if (!Boolean.TRUE.equals(user.getIsInstructorApproved())) {

                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,

                            "Instructor role not approved");

                }

                return;

            }

        }



        // ✅ INSTRUCTOR → USER

        if (currentRole == User.Role.INSTRUCTOR) {

            if (targetRole == User.Role.STUDENT) {

                return;

            }

        }



        // ❌ Everything else blocked

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,

                "Role switch not allowed from " +

                        toJwtRole(currentRole) + " to " + toJwtRole(targetRole));

    }



    private boolean isAdminRole(String role) {

        return role != null && role.toUpperCase().contains("ADMIN");

    }



    private String toJwtRole(User.Role role) {

        if (role == User.Role.STUDENT) {

            return "USER";

        }

        return role.name();

    }

}