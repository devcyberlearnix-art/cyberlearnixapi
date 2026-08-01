package com.user.register.service;



import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import com.user.register.dto.*;

import com.user.register.entity.InstructorApplication;

import com.user.register.entity.User;

import com.user.register.repository.AuditLogRepository;

import com.user.register.repository.InstructorApplicationRepository;

import com.user.register.repository.OTPCodeRepository;

import com.user.register.repository.UserRepository;

import com.user.register.repository.UserSessionRepository;

import com.user.register.security.JwtUtil;

import com.user.register.security.UnifiedJwtService;

import com.user.register.util.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;



import java.util.ArrayList;

import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;



import javax.imageio.ImageIO;

import java.awt.*;

import java.awt.image.BufferedImage;

import java.io.File;

import java.io.IOException;

import java.nio.file.Files;

import java.nio.file.Paths;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Map;

import java.util.Optional;

import java.util.UUID;

import java.util.stream.Collectors;



@Service

public class UserService {



    private final UserRepository userRepository;

    private final UserSessionRepository sessionRepository;

    private final InstructorApplicationRepository instructorApplicationRepository;

    private final UnifiedJwtService unifiedJwtService;

    private final String encryptionKey = "1234567890123456"; // your encryption key

    private final OTPCodeRepository otpCodeRepository;

    private byte[] secretKey;

    private AuditLogRepository auditLogRepository;

    private final Cloudinary cloudinary;



    @Value("${cloudinary.folder:cyberlearnix}")

    private String folder;



    public UserService(UserRepository userRepository,

                       UserSessionRepository sessionRepository,

                       InstructorApplicationRepository instructorApplicationRepository,

                       UnifiedJwtService unifiedJwtService,

                       AuditLogRepository auditLogRepository,

                       OTPCodeRepository otpCodeRepository,

                       Cloudinary cloudinary) {

        this.userRepository = userRepository;

        this.sessionRepository = sessionRepository;

        this.instructorApplicationRepository = instructorApplicationRepository;

        this.unifiedJwtService = unifiedJwtService;

        this.auditLogRepository = auditLogRepository;

        this.otpCodeRepository = otpCodeRepository;

        this.cloudinary = cloudinary;

    }



    /**

     * Resolves the authenticated user's UUID.

     * Primary: SecurityContext (populated by UnifiedJwtAuthenticationFilter from gateway X-User-Id header or JWT).

     * Fallback: parse JWT from Authorization header (for direct service access).

     */

    private UUID resolveAuthenticatedUserId(HttpServletRequest request) {

        // 1️⃣ Try SecurityContext first (set by UnifiedJwtAuthenticationFilter)

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String principal) {

            try {

                return UUID.fromString(principal);

            } catch (IllegalArgumentException ignored) {

                // principal is not a UUID — fall through to JWT parsing

            }

        }



        // 2️⃣ Fallback: parse JWT from Authorization header using UnifiedJwtService

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {

                String userIdStr = unifiedJwtService.extractUserId(token);

                return UUID.fromString(userIdStr);

            } catch (Exception e) {

                throw new RuntimeException("Invalid JWT token: " + e.getMessage());

            }

        }



        throw new RuntimeException("Missing authentication: no SecurityContext or Authorization header");

    }



    public UserProfileResponse getLoggedInUserProfile(HttpServletRequest request) {

        // 1️⃣ Get authenticated user ID

        UUID userId = resolveAuthenticatedUserId(request);

        

        // 2️⃣ Try to fetch user from local DB first

        Optional<User> userOptional = userRepository.findById(userId);

        

        if (userOptional.isPresent()) {

            // User exists in local database (students, instructors)

            User user = userOptional.get();

            return buildUserProfileFromUser(user);

        } else {

            // User not in local database - might be admin from admin database

            return fetchAdminProfileFromAdminService(userId, request);

        }

    }

    

    private UserProfileResponse buildUserProfileFromUser(User user) {

        // Decrypt fields before returning

        String firstName = decrypt(user.getFirstName());

        String lastName = decrypt(user.getLastName());

        String mobile = decrypt(user.getMobile());

        String dob = decrypt(user.getDob());

        String city = decrypt(user.getCity());

        String state = decrypt(user.getState());

        String country = decrypt(user.getCountry());

        String organization = decrypt(user.getOrganization());





        List<SessionDto> activeSessions = sessionRepository.findByUser(user)

                .stream()

                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))

                .map(s -> new SessionDto(

                        s.getId(),

                        user.getId(),   // ✅ UUID safe

                        s.getDeviceInfo(),

                        s.getIpAddress(),

                        s.getCreatedAt(),

                        user.getEmail()

                ))

                .collect(Collectors.toList());

        // 5️⃣ Build profile response

        return new UserProfileResponse(

                firstName,

                lastName,

                user.getEmail(),

                mobile,

                dob,

                user.getProfilePhoto(),

                city,

                state,

                country,

                user.getPreferredLanguage(),

                organization,

                user.getSkills(),

                user.getFieldOfStudy(),

                user.getHighestQualification(),

                user.getId(),              // ✅ UUID HERE

                user.getEffectiveRole(),       // ✅ dynamic role

                user.getStatus().name(),

                user.getCreatedAt(),

                user.getUpdatedAt(),

                user.getLastLoginAt(),

                activeSessions

        );

    }

    

    private UserProfileResponse fetchAdminProfileFromAdminService(UUID userId, HttpServletRequest request) {

        try {

            // Get JWT token from request

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                throw new RuntimeException("Missing Authorization header");

            }

            String token = authHeader.substring(7);

            

            // Extract email from JWT

            String email = unifiedJwtService.extractEmail(token);

            String role = unifiedJwtService.extractRole(token);

            String adminType = unifiedJwtService.extractAdminType(token);

            String assignedService = unifiedJwtService.extractAssignedService(token);

            

            // Build profile response from JWT claims (admin data is in admin database)

            return new UserProfileResponse(

                    "Admin",  // firstName

                    "User",   // lastName

                    email,

                    "",       // mobile

                    "",       // dob

                    "",       // profilePhoto

                    "",       // city

                    "",       // state

                    "",       // country

                    "en",     // preferredLanguage

                    "",       // organization

                    "",       // skills

                    "",       // fieldOfStudy

                    "",       // highestQualification

                    userId,

                    role,

                    "ACTIVE",

                    null,     // createdAt

                    null,     // updatedAt

                    null,     // lastLoginAt

                    new ArrayList<>()  // activeSessions

            );

        } catch (Exception e) {

            throw new RuntimeException("Failed to fetch admin profile: " + e.getMessage());

        }

    }



    // ----------------------------

    // Decrypt helper using your SecurityUtils

    private String decrypt(String value) {

        if (value == null) return null;

        try {

            return SecurityUtils.decrypt(value, encryptionKey);

        } catch (Exception e) {

            // If decryption fails, return original encrypted value as fallback

            return value;

        }

    }



    public UserProfileResponse updateUserProfile(HttpServletRequest request, UpdateUserProfileRequest updateRequest) {

        // 1️⃣ Get authenticated user ID

        UUID userId = resolveAuthenticatedUserId(request);

        // 2️⃣ Fetch user from DB

        User user = userRepository.findById(userId)

                .orElseThrow(() -> new RuntimeException("User not found"));

        try {

            // 3️⃣ Update fields if present and encrypt

            if (updateRequest.getFirstName() != null)

                user.setFirstName(SecurityUtils.encrypt(updateRequest.getFirstName(), encryptionKey));

            if (updateRequest.getLastName() != null)

                user.setLastName(SecurityUtils.encrypt(updateRequest.getLastName(), encryptionKey));

            if (updateRequest.getMobile() != null)

                user.setMobile(SecurityUtils.encrypt(updateRequest.getMobile(), encryptionKey));

            if (updateRequest.getDob() != null)

                user.setDob(SecurityUtils.encrypt(updateRequest.getDob(), encryptionKey));

            if (updateRequest.getCity() != null)

                user.setCity(SecurityUtils.encrypt(updateRequest.getCity(), encryptionKey));

            if (updateRequest.getState() != null)

                user.setState(SecurityUtils.encrypt(updateRequest.getState(), encryptionKey));

            if (updateRequest.getCountry() != null)

                user.setCountry(SecurityUtils.encrypt(updateRequest.getCountry(), encryptionKey));

            if (updateRequest.getOrganization() != null)

                user.setOrganization(SecurityUtils.encrypt(updateRequest.getOrganization(), encryptionKey));

        } catch (Exception e) {

            throw new RuntimeException("Error encrypting user fields: " + e.getMessage(), e);

        }



        // 4️⃣ Update other fields

        if (updateRequest.getPreferredLanguage() != null)

            user.setPreferredLanguage(updateRequest.getPreferredLanguage());

        if (updateRequest.getProfilePhoto() != null) user.setProfilePhoto(updateRequest.getProfilePhoto());

        if (updateRequest.getSkills() != null) user.setSkills(updateRequest.getSkills());

        if (updateRequest.getFieldOfStudy() != null) user.setFieldOfStudy(updateRequest.getFieldOfStudy());

        if (updateRequest.getHighestQualification() != null)

            user.setHighestQualification(updateRequest.getHighestQualification());



        // 5️⃣ Save user

        userRepository.save(user);



        // 6️⃣ Build simplified response (decrypted fields only)

        try {

            return UserProfileResponse.builder()

                    .userId(user.getId())

                    .firstName(decrypt(user.getFirstName()))

                    .lastName(decrypt(user.getLastName()))

                    .email(user.getEmail())

                    .mobile(decrypt(user.getMobile()))

                    .dob(decrypt(user.getDob()))

                    .profilePhoto(user.getProfilePhoto())

                    .city(decrypt(user.getCity()))

                    .state(decrypt(user.getState()))

                    .country(decrypt(user.getCountry()))

                    .preferredLanguage(user.getPreferredLanguage())

                    .organization(decrypt(user.getOrganization()))

                    .skills(user.getSkills())

                    .fieldOfStudy(user.getFieldOfStudy())

                    .highestQualification(user.getHighestQualification())

                    .role(user.getRole().name())

                    .status(user.getStatus().name())

                    .createdAt(user.getCreatedAt())

                    .updatedAt(user.getUpdatedAt())

                    .lastLogin(user.getLastLoginAt())

                    .activeSessions(null)

                    .build();

        } catch (Exception e) {

            throw new RuntimeException("Error decrypting user fields: " + e.getMessage(), e);

        }

    }



    public UserProfileResponse uploadProfilePhoto(HttpServletRequest request, MultipartFile file) {

        // 1️⃣ Get authenticated user ID

        UUID userId = resolveAuthenticatedUserId(request);

        // 2️⃣ Fetch user

        User user = userRepository.findById(userId)

                .orElseThrow(() -> new RuntimeException("User not found"));



        // 3️⃣ Validate file

        if (file.isEmpty()) {

            throw new RuntimeException("No file uploaded");

        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB

            throw new RuntimeException("File size exceeds 5MB limit");

        }



        String contentType = file.getContentType();

        if (contentType == null ||

                !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {

            throw new RuntimeException("Only JPG, PNG, or WEBP files are allowed");

        }

        try {

            // 4️⃣ Resize to 512x512

            BufferedImage originalImage = ImageIO.read(file.getInputStream());

            BufferedImage resizedImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = resizedImage.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g.drawImage(originalImage, 0, 0, 512, 512, null);

            g.dispose();

            // 5️⃣ Save file to byte array and upload to Cloudinary

            String extension = contentType.equals("image/jpeg") ? "jpg" : contentType.split("/")[1];

            String filename = UUID.randomUUID().toString();

            

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ImageIO.write(resizedImage, extension, os);

            byte[] fileBytes = os.toByteArray();



            Map<?, ?> options = ObjectUtils.asMap(

                    "folder", folder,

                    "public_id", filename,

                    "resource_type", "image"

            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(fileBytes, options);

            String fileUrl = (String) uploadResult.get("secure_url");



            // 6️⃣ Update user

            user.setProfilePhoto(fileUrl);

            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            // 7️⃣ Fetch active sessions

            List<SessionDto> activeSessions = sessionRepository.findByUser(user)

                    .stream()

                    .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))

                    .map(s -> new SessionDto(

                            s.getId(),

                            user.getId(),



                            s.getDeviceInfo(),

                            s.getIpAddress(),

                            s.getCreatedAt(),

                            user.getEmail()

                    ))

                    .collect(Collectors.toList());

            // 8️⃣ Return simplified response

            return UserProfileResponse.builder()

                    .userId(user.getId())

                    .firstName(decrypt(user.getFirstName()))

                    .lastName(decrypt(user.getLastName()))

                    .email(user.getEmail())

                    .mobile(decrypt(user.getMobile()))

                    .dob(decrypt(user.getDob()))

                    .profilePhoto(user.getProfilePhoto())

                    .city(decrypt(user.getCity()))

                    .state(decrypt(user.getState()))

                    .country(decrypt(user.getCountry()))

                    .preferredLanguage(user.getPreferredLanguage())

                    .organization(decrypt(user.getOrganization()))

                    .skills(user.getSkills())

                    .fieldOfStudy(user.getFieldOfStudy())

                    .highestQualification(user.getHighestQualification())

                    .role(user.getRole().name())

                    .status(user.getStatus().name())

                    .createdAt(user.getCreatedAt())

                    .updatedAt(user.getUpdatedAt())

                    .lastLogin(user.getLastLoginAt())

                    .activeSessions(null)

                    .build();

        } catch (IOException e) {

            throw new RuntimeException("Failed to process uploaded image: " + e.getMessage(), e);

        }

    }



    public ApiResponse<UserProfileResponse> softDeleteUser(HttpServletRequest request) {

        // 1️⃣ Get authenticated user ID

        UUID userId = resolveAuthenticatedUserId(request);

        // 2️⃣ Fetch user

        User user = userRepository.findById(userId)

                .orElseThrow(() -> new RuntimeException("User not found"));



        // 3️⃣ Soft delete: mark user as SUSPENDED (DB allowed value)

        user.setStatus(User.Status.SUSPENDED);

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);



        UserProfileResponse profile = new UserProfileResponse(

                decrypt(user.getFirstName()),

                decrypt(user.getLastName()),

                user.getEmail(),

                decrypt(user.getMobile()),

                decrypt(user.getDob()),

                user.getProfilePhoto(),

                decrypt(user.getCity()),

                decrypt(user.getState()),

                decrypt(user.getCountry()),

                user.getPreferredLanguage(),

                decrypt(user.getOrganization()),

                user.getSkills(),

                user.getFieldOfStudy(),

                user.getHighestQualification(),

                user.getId(),   // ✅ UUID (FIXED HERE)

                user.getRole().name(),

                user.getStatus().name(),

                user.getCreatedAt(),

                user.getUpdatedAt(),

                null,

                null

        );



        // 5️⃣ Return detailed ApiResponse

        return new ApiResponse<>(

                true,

                "Account has been deactivated successfully",

                profile,

                LocalDateTime.now()

        );

    }



    public User socialLogin(String email, String provider) {

        // 1️⃣ Check if user exists

        Optional<User> userOpt = userRepository.findByEmail(email);

        User user;



        if (userOpt.isPresent()) {

            user = userOpt.get();

        } else {

            // 2️⃣ Create new user for social login

            user = new User();

            user.setEmail(email);



            // Use an existing status like PENDING_VERIFICATION or create SOCIAL_LOGIN in enum

            user.setStatus(User.Status.PENDING_VERIFICATION);



            user.setRole(User.Role.STUDENT);



            // Store the provider (Google, GitHub, LinkedIn)

            user.setProvider(provider);



            userRepository.save(user);

        }



        // 3️⃣ Return user object (later JWT or session can be generated)

        return user;

    }



    public List<User> getAllUsers() {

        return userRepository.findAll();

    }



    public List<UserProfileResponse> getAllUsersProfiles() {

        return userRepository.findAll().stream()

                .map(user -> UserProfileResponse.builder()

                        .userId(user.getId())

                        .firstName(decrypt(user.getFirstName()))

                        .lastName(decrypt(user.getLastName()))

                        .email(user.getEmail())

                        .mobile(decrypt(user.getMobile()))

                        .dob(decrypt(user.getDob()))

                        .profilePhoto(user.getProfilePhoto())

                        .city(decrypt(user.getCity()))

                        .state(decrypt(user.getState()))

                        .country(decrypt(user.getCountry()))

                        .preferredLanguage(user.getPreferredLanguage())

                        .organization(decrypt(user.getOrganization()))

                        .skills(user.getSkills())

                        .fieldOfStudy(user.getFieldOfStudy())

                        .highestQualification(user.getHighestQualification())

                        .role(user.getRole().name())

                        .status(user.getStatus().name())

                        .createdAt(user.getCreatedAt())

                        .updatedAt(user.getUpdatedAt())

                        .lastLogin(user.getLastLoginAt())

                        .build())

                .collect(Collectors.toList());

    }



    public List<User> getAllInstructors() {

        return userRepository.findByRole(User.Role.INSTRUCTOR);

    }



    public UserProfileResponse getUserById(UUID id) {

        User user = userRepository.findById(id)

                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));



        return UserProfileResponse.builder()

                .userId(user.getId())   // UUID goes here

                .firstName(decrypt(user.getFirstName()))

                .lastName(decrypt(user.getLastName()))

                .email(user.getEmail())

                .mobile(decrypt(user.getMobile()))

                .dob(decrypt(user.getDob()))

                .profilePhoto(user.getProfilePhoto())

                .city(decrypt(user.getCity()))

                .state(decrypt(user.getState()))

                .country(decrypt(user.getCountry()))

                .preferredLanguage(user.getPreferredLanguage())

                .organization(decrypt(user.getOrganization()))

                .skills(user.getSkills())

                .fieldOfStudy(user.getFieldOfStudy())

                .highestQualification(user.getHighestQualification())

                .role(user.getRole().name())

                .status(user.getStatus().name())

                .createdAt(user.getCreatedAt())

                .updatedAt(user.getUpdatedAt())

                .lastLogin(user.getLastLoginAt())

                .build();

    }

    public UserProfileResponse updateUserStatus(UUID id, String status) {



        User user = userRepository.findById(id)

                .orElseThrow(() -> new RuntimeException("User not found"));



        if ("REJECTED".equalsIgnoreCase(status)) {

            user.setApplicationStatus(User.ApplicationStatus.REJECTED);

            user.setIsInstructorApproved(false);

            syncInstructorApplication(id, com.user.register.entity.InstructorApplication.ApplicationStatus.REJECTED);

        } else if ("ACTIVE".equalsIgnoreCase(status)) {

            user.setApplicationStatus(User.ApplicationStatus.APPROVED);

            user.setIsInstructorApproved(true);

            user.setRole(User.Role.INSTRUCTOR);

            syncInstructorApplication(id, com.user.register.entity.InstructorApplication.ApplicationStatus.APPROVED);

        }

        // ✅ fallback (normal user status)

        else {

            user.setStatus(User.Status.valueOf(status.toUpperCase()));

        }



        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);



        return getUserById(id);

    }

    @Transactional

    public void deleteUserById(UUID id) {



        User user = userRepository.findById(id)

                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));



        sessionRepository.deleteByUser(user);   // ✅

        auditLogRepository.deleteByUser(user);  // ✅

        otpCodeRepository.deleteByUser(user);   // 🔥 ADD THIS



        userRepository.delete(user);            // ✅ now works

    }



    private void syncInstructorApplication(UUID userId, InstructorApplication.ApplicationStatus status) {

        instructorApplicationRepository.findTopByUserIdOrderBySubmittedAtDesc(userId)

                .ifPresent(app -> {

                    app.setStatus(status);

                    app.setReviewedAt(LocalDateTime.now());

                    instructorApplicationRepository.save(app);

                });

    }



    public Map<String, Object> getUserStats() {

        long totalUsers = userRepository.count();

        long activeUsers = userRepository.countByStatus(User.Status.ACTIVE);

        long lockedUsers = userRepository.countByStatus(User.Status.LOCKED);

        long suspendedUsers = userRepository.countByStatus(User.Status.SUSPENDED);

        long pendingUsers = userRepository.countByStatus(User.Status.PENDING_VERIFICATION);

        long socialLoginUsers = userRepository.countByStatus(User.Status.SOCIAL_LOGIN);

        long deletedUsers = userRepository.countByStatus(User.Status.DELETED);

        

        long totalStudents = userRepository.countByRole(User.Role.STUDENT);
        long totalInstructors = userRepository.countByRole(User.Role.INSTRUCTOR);
        long totalMainAdmins = userRepository.countByRole(User.Role.MAIN_ADMIN);
        long totalSubAdmins = userRepository.countByRole(User.Role.SUB_ADMIN);
        long totalAdmins = totalMainAdmins + totalSubAdmins;



        // Calculate new users this month

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);



        return Map.of(

                "totalUsers", totalUsers,

                "totalStudents", totalStudents,

                "totalInstructors", totalInstructors,

                "totalAdmins", totalAdmins,

                "statusBreakdown", Map.of(

                        "active", activeUsers,

                        "pendingVerification", pendingUsers,

                        "socialLogins", socialLoginUsers,

                        "locked", lockedUsers,

                        "suspended", suspendedUsers,

                        "deleted", deletedUsers

                ),

                "growthMetrics", Map.of(

                        "newUsersThisMonth", newUsersThisMonth // placeholder logic

                )

        );

    }

}