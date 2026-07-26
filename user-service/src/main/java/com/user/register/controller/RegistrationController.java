package com.user.register.controller;

import com.user.register.dto.*;
import com.user.register.entity.User;
import com.user.register.entity.UserSession;
import com.user.register.exception.InvalidOtpException;
import com.user.register.exception.LoginFailedException;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.service.RegistrationService;
import com.user.register.service.SessionService;
import com.user.register.service.TokenBlacklistService;
import com.user.register.util.SecurityUtils;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.security.auth.login.AccountLockedException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    private final UserRepository userRepository;           // add this
    private final UserSessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;
    private final SessionService sessionService;
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:cyberlearnix}")
    private String folder;

    private final String encryptionKey = "my-secret-key";


    /**
     * Register user and send OTP
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user, HttpServletRequest request) {
        System.out.println("=== REGISTER CONTROLLER CALLED ===");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Mobile: " + user.getMobile());
        System.out.println("CountryCode: " + user.getCountryCode());
        try {

            // Force role to STUDENT
            user.setRole(User.Role.STUDENT);
            user.setIsInstructorApproved(false);

            // Ensure profile photo is set if provided
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) {
                user.setProfilePhoto(user.getProfilePhoto());
            }

            // Pass request to service
            User savedUser = registrationService.register(user, request);

            ApiResponse<User> response = new ApiResponse<>(
                    true,
                    "User registered successfully. OTP has been sent to email.",
                    savedUser,
                    LocalDateTime.now()
            );

            // 201 → resource created
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {

            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );

            // 400 → validation error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println("=== REGISTRATION RUNTIME EXCEPTION ===");
            System.out.println("Message: " + e.getMessage());
            System.out.println("Cause: " + e.getCause());
            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );

            // 409 → conflict (email/mobile already exists)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {

            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    "Internal server error",
                    null,
                    LocalDateTime.now()
            );

            // 500 → server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        String email = body.get("email");
        String otp = body.get("otp");

        try {
            // Call service (pass response for cookies)
            ResponseEntity<Map<String, Object>> serviceResponse =
                    registrationService.verifyOTP(email, otp, request, response);
            return serviceResponse;
        } catch (InvalidOtpException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("data", Map.of(
                    "remainingAttempts", ex.getRemainingAttempts(),
                    "expiresInSeconds", ex.getSecondsUntilExpiry()
            ));
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", Map.of(
                    "remainingAttempts", 0,
                    "expiresInSeconds", 0
            ));
            errorResponse.put("timestamp", LocalDateTime.now());

            String msg = e.getMessage().toLowerCase();
            HttpStatus status;

            if (msg.contains("locked")) {
                status = HttpStatus.LOCKED; // 423
            } else if (msg.contains("suspended")) {
                status = HttpStatus.FORBIDDEN; // 403
            } else if (msg.contains("not found")) {
                status = HttpStatus.NOT_FOUND; // 404
            } else if (msg.contains("expired")) {
                status = HttpStatus.BAD_REQUEST; // 400
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
            }
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    // Disabled - using unified authentication endpoint instead
    // @PostMapping("/login")
    // public ResponseEntity<?> loginWithPassword(@RequestBody LoginRequest request,
    //                                            HttpServletRequest httpRequest,
    //                                            HttpServletResponse httpResponse) {
    //
    //     try {
    //         // 1️⃣ Authenticate user via service
    //         ResponseEntity<Map<String, Object>> loginResponse =
    //                 registrationService.loginWithPassword(request, httpRequest, httpResponse);
    //
    //         // 2️⃣ Fetch user: Check if this was an Admin login (user not present in user-service DB)
    //         java.util.Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
    //         if (optionalUser.isEmpty()) {
    //             Map<String, Object> adminBody = loginResponse.getBody();
    //             if (adminBody != null) {
    //                 Map<String, Object> adminInfo = (Map<String, Object>) adminBody.get("admin");
    //                 Map<String, Object> authInfo = (Map<String, Object>) adminBody.get("authentication");
    //                 Map<String, Object> sessionInfo = (Map<String, Object>) adminBody.get("sessionInfo");
    //
    //                 String adminAccessToken = authInfo != null ? (String) authInfo.get("accessToken") : null;
    //                 String adminRefreshToken = authInfo != null ? (String) authInfo.get("refreshToken") : null;
    //
    //                 UUID adminId = adminInfo != null && adminInfo.get("id") != null ? UUID.fromString((String) adminInfo.get("id")) : null;
    //                 String email = adminInfo != null ? (String) adminInfo.get("email") : null;
    //                 String role = adminInfo != null ? (String) adminInfo.get("role") : null;
    //
    //                 String firstName = adminInfo != null ? (String) adminInfo.get("firstName") : "Admin";
    //                 String lastName = adminInfo != null ? (String) adminInfo.get("lastName") : "";
    //                 String mobileNumber = adminInfo != null ? (String) adminInfo.get("mobileNumber") : "";
    //
    //                 // Setup Cookies
    //                 if (adminAccessToken != null) {
    //                     Cookie accessCookie = new Cookie("accessToken", adminAccessToken);
    //                     accessCookie.setHttpOnly(true);
    //                     accessCookie.setSecure(false);
    //                     accessCookie.setPath("/");
    //                     accessCookie.setMaxAge(60 * 60); // 1 hour for admin
    //                     httpResponse.addCookie(accessCookie);
    //                 }
    //
    //                 if (adminRefreshToken != null) {
    //                     Cookie refreshCookie = new Cookie("refreshToken", adminRefreshToken);
    //                     refreshCookie.setHttpOnly(true);
    //                     refreshCookie.setSecure(false);
    //                     refreshCookie.setPath("/");
    //                     refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days for admin
    //                     httpResponse.addCookie(refreshCookie);
    //                 }
    //
    //                 LoginResponse responseData = new LoginResponse();
    //                 responseData.setAccessToken(adminAccessToken);
    //                 responseData.setRefreshToken(adminRefreshToken);
    //                 responseData.setExpiresInSeconds(3600); // 1 hour
    //                 responseData.setTokenType("Bearer");
    //                 responseData.setActiveRole(role); // e.g. "MAIN_ADMIN" or "SUB_ADMIN"
    //
    //                 responseData.setUserId(adminId);
    //                 responseData.setEmail(encryptNullable(email));
    //                 responseData.setFirstName(encryptNullable(firstName));
    //                 responseData.setLastName(encryptNullable(lastName));
    //                 responseData.setMobile(encryptNullable(mobileNumber));
    //                 responseData.setRole(encryptNullable(role));
    //                 responseData.setStatus(encryptNullable("ACTIVE"));
    //
    //                 // Add device/IP details if available
    //                 if (sessionInfo != null) {
    //                     responseData.setLoginDevice(encryptNullable((String) sessionInfo.get("device")));
    //                     responseData.setLoginIp(encryptNullable((String) sessionInfo.get("ipAddress")));
    //                 }
    //                 responseData.setLastLoginAt(LocalDateTime.now());
    //
    //                 ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(
    //                         true,
    //                         "Login successful",
    //                         responseData,
    //                         LocalDateTime.now()
    //                 );
    //                 return ResponseEntity.ok(apiResponse);
    //             }
    //             return loginResponse;
    //         }
    //
    //         User user = optionalUser.get();
    //
    //         // Invalidate all old sessions for this user (enforce single device)
    //         sessionService.invalidateAllSessionsForUser(user);
    //
    //         // 3️⃣ Generate tokens using JwtUtil
    //         String jwtRole = user.getRole() == User.Role.STUDENT ? "USER" : user.getRole().name();
    //         String accessToken = jwtUtil.generateAccessToken(user.getId().toString(), jwtRole);
    //         String refreshToken = jwtUtil.generateRefreshToken(user, user.getId().toString(), jwtRole);
    //
    //
    //         LocalDateTime now = LocalDateTime.now();
    //         LocalDateTime accessTokenExpiry = now.plusMinutes(15);
    //         LocalDateTime refreshTokenExpiry = now.plusDays(30);
    //
    //         // 4️⃣ Get device + IP
    //         String deviceInfo = httpRequest.getHeader("User-Agent");
    //         if (deviceInfo == null) deviceInfo = "Unknown Device";
    //
    //         String ipAddress = httpRequest.getRemoteAddr();
    //
    //         // 5️⃣ Save session
    //         UserAgent userAgent = UserAgent.parseUserAgentString(deviceInfo);
    //         String browser = userAgent.getBrowser().getName();
    //         String os = userAgent.getOperatingSystem().getName();
    //         UserSession session = UserSession.builder()
    //                 .user(user)
    //                 .accessToken(accessToken)    // ✅ set accessToken
    //                 .refreshToken(refreshToken)
    //                 .deviceInfo(deviceInfo)
    //                 .ipAddress(ipAddress)
    //                 .expiresAt(refreshTokenExpiry)
    //                 .build();
    //         sessionRepository.save(session);
    //
    //         // 6️⃣ Set HttpOnly cookies for tokens
    //         Cookie accessCookie = new Cookie("accessToken", accessToken);
    //         accessCookie.setHttpOnly(true);
    //         accessCookie.setSecure(false);
    //         accessCookie.setPath("/");
    //         accessCookie.setMaxAge(15 * 60); // 15 mins
    //         httpResponse.addCookie(accessCookie);
    //
    //         Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
    //         refreshCookie.setHttpOnly(true);
    //         refreshCookie.setSecure(false);
    //         refreshCookie.setPath("/");
    //         refreshCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
    //         httpResponse.addCookie(refreshCookie);
    //
    //         // 7️⃣ Prepare encrypted response
    //         LoginResponse responseData = new LoginResponse();
    //
    //         responseData.setAccessToken(accessToken);
    //         responseData.setRefreshToken(refreshToken);
    //         responseData.setExpiresInSeconds(900);
    //         responseData.setTokenType("Bearer");
    //         responseData.setActiveRole(jwtRole);
    //
    //         responseData.setUserId(user.getId());
    //
    //         responseData.setEmail(encryptNullable(user.getEmail()));
    //         responseData.setFirstName(encryptNullable(user.getFirstName()));
    //         responseData.setLastName(encryptNullable(user.getLastName()));
    //         responseData.setMobile(encryptNullable(user.getMobile()));
    //
    //         responseData.setDob(encryptNullable(user.getDob()));
    //         responseData.setProfilePhoto(encryptNullable(user.getProfilePhoto()));
    //         responseData.setCity(encryptNullable(user.getCity()));
    //         responseData.setState(encryptNullable(user.getState()));
    //         responseData.setCountry(encryptNullable(user.getCountry()));
    //         responseData.setOrganization(encryptNullable(user.getOrganization()));
    //
    //         responseData.setPreferredLanguage(encryptNullable(user.getPreferredLanguage()));
    //         responseData.setSkills(encryptNullable(user.getSkills()));
    //         responseData.setFieldOfStudy(encryptNullable(user.getFieldOfStudy()));
    //         responseData.setHighestQualification(encryptNullable(user.getHighestQualification()));
    //
    //         responseData.setRole(encryptNullable(user.getRole() != null ? user.getRole().name() : null));
    //         responseData.setStatus(encryptNullable(user.getStatus() != null ? user.getStatus().name() : null));
    //
    //         responseData.setLoginDevice(encryptNullable(deviceInfo));
    //         responseData.setLoginIp(encryptNullable(ipAddress));
    //         responseData.setBrowser(encryptNullable(browser));
    //         responseData.setOs(encryptNullable(os));
    //         responseData.setLastLoginAt(LocalDateTime.now());
    //         responseData.setSessionId(String.valueOf(session.getId()));
    //         // 8️⃣ API Response
    //         ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(
    //                 true,
    //                 "Login successful",
    //                 responseData,
    //                 now
    //         );
    //         return ResponseEntity.ok(apiResponse);
    //
    //     } catch (LoginFailedException ex) {
    //         ApiResponse<Object> response = new ApiResponse<>(
    //                 false,
    //                 ex.getMessage(),
    //                 ex.getDetails(),
    //                 LocalDateTime.now()
    //         );
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    //     } catch (AccountLockedException ex) {
    //         ApiResponse<Object> response = new ApiResponse<>(
    //                 false,
    //                 "Account locked due to multiple failed login attempts",
    //                 null,
    //                 LocalDateTime.now()
    //         );
    //         return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    //     } catch (ResponseStatusException ex) {
    //         ApiResponse<Object> response = new ApiResponse<>(
    //                 false,
    //                 ex.getReason() != null ? ex.getReason() : ex.getMessage(),
    //                 null,
    //                 LocalDateTime.now()
    //         );
    //         return ResponseEntity.status(ex.getStatusCode()).body(response);
    //     } catch (RuntimeException e) {
    //         ApiResponse<Object> response = new ApiResponse<>(
    //                 false,
    //                 e.getMessage(),
    //                 null,
    //                 LocalDateTime.now()
    //             );
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    //     } catch (Exception e) {
    //         throw new RuntimeException(e);
    //     }
    // }

    private String encryptNullable(String value) throws Exception {
        if (value == null) {
            return null;
        }
        return SecurityUtils.encrypt(value, encryptionKey);
    }

    // Disabled - using unified authentication endpoint instead
    // @PostMapping("/login/otp/request")
    // public ResponseEntity<?> requestLoginOtp(@RequestBody Map<String, String> request) {
    //     try {
    //         String email = request.get("email");
    //
    //         // 🔹 Get detailed response from service
    //         ApiResponse<Map<String, Object>> response =
    //                 registrationService.sendLoginOtp(email);
    //
    //         return ResponseEntity.ok(response);
    //
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    //                 .body(new ApiResponse<>(
    //                         false,
    //                         e.getMessage(),
    //                         null,
    //                         LocalDateTime.now()
    //                 ));
    //     }
    // }

    // Disabled - using unified authentication endpoint instead
    // @PostMapping("/login/otp/verify")
    // public ResponseEntity<ApiResponse<?>> verifyLoginOtp(@RequestBody Map<String, String> request) {
    //
    //     String email = request.get("email");
    //     String otp = request.get("otp");
    //
    //     // Call service method which returns ApiResponse
    //     ApiResponse<?> response = registrationService.verifyLoginOtp(email, otp);
    //
    //     // Determine HTTP status based on success and message
    //     HttpStatus status = HttpStatus.OK; // default
    //
    //     if (!response.isSuccess()) {
    //         String msg = response.getMessage().toLowerCase();
    //
    //         if (msg.contains("locked")) {
    //             status = HttpStatus.LOCKED; // 423
    //         } else if (msg.contains("invalid otp") || msg.contains("invalid credentials")) {
    //             status = HttpStatus.UNAUTHORIZED; // 401
    //         } else if (msg.contains("not active")) {
    //             status = HttpStatus.FORBIDDEN; // 403
    //         } else if (msg.contains("frequently")) {
    //             status = HttpStatus.TOO_MANY_REQUESTS; // 429
    //         } else {
    //             status = HttpStatus.BAD_REQUEST; // fallback
    //         }
    //     }
    //
    //     return ResponseEntity.status(status).body(response);
    // }

    // Disabled - using unified authentication endpoint instead
    // @PostMapping("/refresh")
    // public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
    //
    //     LocalDateTime now = LocalDateTime.now();
    //
    //     try {
    //         // 1️⃣ Get refresh token from request body
    //         String refreshToken = request.get("refreshToken");
    //
    //         if (refreshToken == null || refreshToken.isBlank()) {
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                     .body(new ApiResponse<>(false, "Missing refresh token in request body", null, now));
    //         }
    //
    //         // 2️⃣ Call service to refresh access token
    //         LoginResponse response = registrationService.refreshAccessToken(refreshToken);
    //
    //         return ResponseEntity.ok(
    //                 new ApiResponse<>(
    //                         true,
    //                         "Access token refreshed successfully.",
    //                         response,
    //                         now
    //                 )
    //         );
    //
    //     } catch (RuntimeException ex) {
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                 .body(new ApiResponse<>(
    //                         false,
    //                         ex.getMessage(),
    //                         null,
    //                         now
    //                 ));
    //     }
    // }

    // ================= FORGOT PASSWORD =================
    // Disabled - using unified authentication endpoint instead
    // @PostMapping("/password/forgot")
    // public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
    //     try {
    //         ForgotPasswordResponseData data = registrationService.sendResetOtp(request.getEmail());
    //         return ResponseEntity.ok(new ApiResponse<>(true, "OTP sent successfully", data, LocalDateTime.now()));
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    //                 .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
    //     }
    // }

    // Disabled - using unified authentication endpoint instead
    // @PostMapping("/password/verify-otp")
    // public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
    //     try {
    //         Map<String, Object> data = registrationService.verifyOtpAndGenerateResetToken(
    //                 request.getEmail(),
    //                 request.getOtp()
    //         );
    //
    //         return ResponseEntity.ok(new ApiResponse<>(true, "OTP verified", data, LocalDateTime.now()));
    //
    //     } catch (InvalidOtpException ex) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    //                 .body(new ApiResponse<>(false, ex.getMessage(), null, LocalDateTime.now()));
    //     } catch (RuntimeException ex) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    //                 .body(new ApiResponse<>(false, ex.getMessage(), null, LocalDateTime.now()));
    //     }
    // }

    // Disabled - using unified authentication endpoint instead
    // @PostMapping("/password/reset")
    // public ResponseEntity<?> resetPassword(
    //         @RequestHeader(value = "Authorization", required = false) String authHeader,
    //         @RequestBody ResetPasswordRequest request) {
    //
    //     try {
    //
    //         Map<String, Object> data = registrationService.resetPasswordWithToken(
    //                 authHeader,
    //                 request.getNewPassword(),
    //                 request.getConfirmPassword()
    //         );
    //
    //         return ResponseEntity.ok(
    //                 new ApiResponse<>(true, "Password reset successful", data, LocalDateTime.now())
    //         );
    //
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.badRequest()
    //                 .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
    //     }
    // }

    // Disabled - using unified authentication endpoint instead
    // @PostMapping("/logout")
    // public ApiResponse<Map<String, Object>> logout(
    //         @RequestHeader(value = "Authorization", required = false) String authHeader,
    //         HttpServletRequest request) {
    //
    //     if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    //         return new ApiResponse<>(
    //                 false,
    //                 "Authorization header missing or invalid",
    //                 null,
    //                 LocalDateTime.now()
    //         );
    //     }
    //     String accessToken = authHeader.substring(7); // remove "Bearer "
    //
    //     // ===================== Friendly device detection =====================
    //     String userAgent = request.getHeader("User-Agent");
    //     String deviceInfo = "Other Device"; // default
    //
    //     if (userAgent != null && !userAgent.isBlank()) {
    //         String uaLower = userAgent.toLowerCase();
    //         if (uaLower.contains("mozilla") || uaLower.contains("chrome") || uaLower.contains("firefox")
    //                 || uaLower.contains("safari") || uaLower.contains("edge")) {
    //             deviceInfo = "Web Browser";
    //         } else if (uaLower.contains("android") || uaLower.contains("iphone") || uaLower.contains("ipad")
    //                 || uaLower.contains("mobile")) {
    //             deviceInfo = "Mobile App";
    //         } else if (uaLower.contains("postman")) {
    //             deviceInfo = "API Client";
    //         }
    //     }
    //
    //     // ===================== Logout in service =====================
    //     Map<String, Object> data = registrationService.logoutCurrentDevice(
    //             accessToken,
    //             request.getRemoteAddr(),
    //             deviceInfo
    //     );
    //
    //     return new ApiResponse<>(
    //             true,
    //             "Logout successful",
    //             data,
    //             LocalDateTime.now()
    //     );
    // }

    @PostMapping("/switch-role")
    public ResponseEntity<?> switchRole(
            @RequestBody SwitchRoleRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            Map<String, Object> data =
                    registrationService.switchRole(request.getSwitchRole(), httpRequest);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Role switched successfully", data, LocalDateTime.now())
            );

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(new ApiResponse<>(false, ex.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal error", null, LocalDateTime.now()));
        }
    }

}