package com.user.register.service;

import com.user.register.service.SessionService;



import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import com.user.register.dto.*;

import com.user.register.entity.InstructorApplication;

import com.user.register.entity.User;

import com.user.register.exception.ProfilePhotoException;

import com.user.register.repository.AuditLogRepository;

import com.user.register.repository.InstructorApplicationRepository;

import com.user.register.repository.UserRepository;

import com.user.register.repository.UserSessionRepository;

import com.user.register.security.JwtUtil;

import com.user.register.security.UnifiedJwtService;

import com.user.register.util.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;

import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class UserService {



    private final UserRepository userRepository;

    private final UserSessionRepository sessionRepository;

    private final InstructorApplicationRepository instructorApplicationRepository;

    private final UnifiedJwtService unifiedJwtService;

    private final String encryptionKey = "1234567890123456"; // your encryption key

    private byte[] secretKey;

    private AuditLogRepository auditLogRepository;

    private final GoogleDriveService googleDriveService;

    private final SessionService sessionService;

    private final TokenBlacklistService tokenBlacklistService;

    public UserService(UserRepository userRepository,

                       UserSessionRepository sessionRepository,

                       InstructorApplicationRepository instructorApplicationRepository,

                       UnifiedJwtService unifiedJwtService,

                       AuditLogRepository auditLogRepository,

                       GoogleDriveService googleDriveService,

                       SessionService sessionService,

                       TokenBlacklistService tokenBlacklistService) {

        this.userRepository = userRepository;

        this.sessionRepository = sessionRepository;

        this.instructorApplicationRepository = instructorApplicationRepository;

        this.unifiedJwtService = unifiedJwtService;

        this.auditLogRepository = auditLogRepository;

        this.googleDriveService = googleDriveService;

        this.sessionService = sessionService;

        this.tokenBlacklistService = tokenBlacklistService;

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

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Re-throw as-is so the controller returns a clear 401 "token expired"
                throw e;
            } catch (io.jsonwebtoken.JwtException e) {
                // Re-throw as-is for a clear 401 "invalid token"
                throw e;
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

            if (user.getStatus() == User.Status.SUSPENDED || user.getStatus() == User.Status.DELETED) {

                throw new RuntimeException("Account has been deleted or suspended");

            }

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

                .map(s -> SessionDto.builder()
                        .id(s.getId())
                        .userId(user.getId())
                        .email(user.getEmail())
                        .deviceInfo(s.getDeviceInfo())
                        .deviceId(s.getDeviceId())
                        .deviceName(s.getDeviceName())
                        .deviceType(s.getDeviceType())
                        .browser(s.getBrowser())
                        .operatingSystem(s.getOperatingSystem())
                        .latitude(s.getLatitude())
                        .longitude(s.getLongitude())
                        .city(s.getCity())
                        .country(s.getCountry())
                        .ipAddress(s.getIpAddress())
                        .loginTime(s.getCreatedAt())
                        .build())

                .collect(Collectors.toList());

        // 5️⃣ Build profile response
        String name = (firstName != null && lastName != null) ? firstName + " " + lastName : null;
        String role = user.getEffectiveRole();
        String profilePhoto = user.getProfilePhoto();

        return UserProfileResponse.builder()

                .firstName(firstName)

                .lastName(lastName)

                .name(name)

                .email(user.getEmail())

                .mobile(mobile)

                .mobileNumber(mobile) // Alias for mobile

                .phone(mobile) // Alias for mobile (can be separated if needed)

                .dob(dob)

                .profilePhoto(profilePhoto)

                .photoURL(profilePhoto) // Deprecated field for backward compatibility

                .avatar(profilePhoto) // Deprecated field for backward compatibility

                .city(city)

                .state(state)

                .country(country)

                .countryCode(user.getCountryCode())

                .preferredLanguage(user.getPreferredLanguage())

                .organization(organization)

                .skills(user.getSkills())

                .fieldOfStudy(user.getFieldOfStudy())

                .highestQualification(user.getHighestQualification())

                .gender(user.getGender())

                .userId(user.getId())

                .id(user.getId().toString()) // String alias for userId

                .role(role)

                .userRole(role) // Alias for role

                .status(user.getStatus().name())

                .createdAt(user.getCreatedAt())

                .updatedAt(user.getUpdatedAt())

                .lastLogin(user.getLastLogin())

                .activeSessions(activeSessions)

                .build();

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
            String name = "Admin User";
            String profilePhoto = "";

            return UserProfileResponse.builder()

                    .firstName("Admin")

                    .lastName("User")

                    .name(name)

                    .email(email)

                    .mobile("")

                    .mobileNumber("")

                    .phone("")

                    .dob("")

                    .profilePhoto(profilePhoto)

                    .photoURL(profilePhoto)

                    .avatar(profilePhoto)

                    .city("")

                    .state("")

                    .country("")

                    .countryCode("")

                    .preferredLanguage("en")

                    .organization("")

                    .skills("")

                    .fieldOfStudy("")

                    .highestQualification("")

                    .gender("")

                    .userId(userId)

                    .id(userId.toString())

                    .role(role)

                    .userRole(role)

                    .status("ACTIVE")

                    .createdAt(null)

                    .updatedAt(null)

                    .lastLogin(null)

                    .activeSessions(new ArrayList<>())

                    .build();

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

        if (user.getStatus() == User.Status.SUSPENDED || user.getStatus() == User.Status.DELETED) {

            throw new RuntimeException("Account has been deleted or suspended");

        }

        try {

            // 3️⃣ Update fields if present and encrypt

            if (updateRequest.getFirstName() != null)

                user.setFirstName(SecurityUtils.encrypt(updateRequest.getFirstName(), encryptionKey));

            if (updateRequest.getLastName() != null)

                user.setLastName(SecurityUtils.encrypt(updateRequest.getLastName(), encryptionKey));

            if (updateRequest.getEmail() != null) {
                // Check if email is already taken by another user
                if (!user.getEmail().equals(updateRequest.getEmail())) {
                    userRepository.findByEmail(updateRequest.getEmail())
                            .ifPresent(existingUser -> {
                                throw new RuntimeException("Email is already in use by another user");
                            });
                    user.setEmail(updateRequest.getEmail());
                }
            }

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

            if (updateRequest.getCountryCode() != null)
                user.setCountryCode(updateRequest.getCountryCode());

            // Note: mobile is already encrypted above (line 494)
            // If countryCode is provided separately, it's stored as-is for reference
            // The mobile field in the database contains the full number with country code

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

        if (updateRequest.getGender() != null)
            user.setGender(updateRequest.getGender());



        // 5️⃣ Save user

        userRepository.save(user);



        // 6️⃣ Build simplified response (decrypted fields only)

        try {
            String firstName = decrypt(user.getFirstName());
            String lastName = decrypt(user.getLastName());
            String mobile = decrypt(user.getMobile());
            String profilePhoto = user.getProfilePhoto();
            String name = (firstName != null && lastName != null) ? firstName + " " + lastName : null;
            String role = user.getRole().name();

            return UserProfileResponse.builder()

                    .userId(user.getId())

                    .id(user.getId().toString())

                    .firstName(firstName)

                    .lastName(lastName)

                    .name(name)

                    .email(user.getEmail())

                    .mobile(mobile)

                    .mobileNumber(mobile)

                    .phone(mobile)

                    .dob(decrypt(user.getDob()))

                    .profilePhoto(profilePhoto)

                    .coverPhoto(user.getCoverPhoto())

                    .photoURL(profilePhoto)

                    .avatar(profilePhoto)

                    .city(decrypt(user.getCity()))

                    .state(decrypt(user.getState()))

                    .country(decrypt(user.getCountry()))

                    .countryCode(user.getCountryCode())

                    .preferredLanguage(user.getPreferredLanguage())

                    .organization(decrypt(user.getOrganization()))

                    .skills(user.getSkills())

                    .fieldOfStudy(user.getFieldOfStudy())

                    .highestQualification(user.getHighestQualification())

                    .gender(user.getGender())

                    .role(role)

                    .userRole(role)

                    .status(user.getStatus().name())

                    .createdAt(user.getCreatedAt())

                    .updatedAt(user.getUpdatedAt())

                    .lastLogin(user.getLastLogin())

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

                .orElseThrow(() -> new ProfilePhotoException(ProfilePhotoException.ErrorCode.USER_NOT_FOUND));



        // 3️⃣ Validate file

        if (file.isEmpty()) {

            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.FILE_EMPTY);

        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB

            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.FILE_TOO_LARGE);

        }



        String contentType = file.getContentType();

        if (contentType == null ||

                !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {

            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.INVALID_FILE_TYPE);

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



            String fileUrl = googleDriveService.uploadBytes(fileBytes, filename + "." + extension, contentType);

            if (fileUrl == null || fileUrl.isEmpty()) {
                throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.UPLOAD_FAILED);
            }

            // 6️⃣ Update user

            user.setProfilePhoto(fileUrl);

            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

        } catch (ProfilePhotoException e) {
            throw e;
        } catch (Exception e) {
            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.UPLOAD_FAILED, e);
        }

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
            String firstName = decrypt(user.getFirstName());
            String lastName = decrypt(user.getLastName());
            String mobile = decrypt(user.getMobile());
            String profilePhoto = user.getProfilePhoto();
            String name = (firstName != null && lastName != null) ? firstName + " " + lastName : null;
            String role = user.getRole().name();

            return UserProfileResponse.builder()

                    .userId(user.getId())

                    .id(user.getId().toString())

                    .firstName(firstName)

                    .lastName(lastName)

                    .name(name)

                    .email(user.getEmail())

                    .mobile(mobile)

                    .mobileNumber(mobile)

                    .phone(mobile)

                    .dob(decrypt(user.getDob()))

                    .profilePhoto(profilePhoto)

                    .coverPhoto(user.getCoverPhoto())

                    .photoURL(profilePhoto)

                    .avatar(profilePhoto)

                    .city(decrypt(user.getCity()))

                    .state(decrypt(user.getState()))

                    .country(decrypt(user.getCountry()))

                    .countryCode(user.getCountryCode())

                    .preferredLanguage(user.getPreferredLanguage())

                    .organization(decrypt(user.getOrganization()))

                    .skills(user.getSkills())

                    .fieldOfStudy(user.getFieldOfStudy())

                    .highestQualification(user.getHighestQualification())

                    .gender(user.getGender())

                    .role(role)

                    .userRole(role)

                    .status(user.getStatus().name())

                    .createdAt(user.getCreatedAt())

                    .updatedAt(user.getUpdatedAt())

                    .lastLogin(user.getLastLogin())

                    .activeSessions(null)

                    .build();

    }

    public UserProfileResponse removeProfilePhoto(HttpServletRequest request) {
        // 1️⃣ Get authenticated user ID
        UUID userId = resolveAuthenticatedUserId(request);

        // 2️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfilePhotoException(ProfilePhotoException.ErrorCode.USER_NOT_FOUND));

        // 3️⃣ Check if user has a profile photo
        if (user.getProfilePhoto() == null || user.getProfilePhoto().isEmpty()) {
            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.NO_PHOTO_TO_REMOVE);
        }

        // 4️⃣ Delete the file from Google Drive
        try {
            String photoUrl = user.getProfilePhoto();
            String fileId = googleDriveService.extractFileId(photoUrl);
            if (fileId != null) {
                googleDriveService.deleteFile(fileId);
            }
        } catch (Exception e) {
            log.error("Failed to delete photo from Google Drive: {}", e.getMessage(), e);
            // Continue with database cleanup even if file deletion fails
        }

        // 5️⃣ Clear the profile photo from user record
        user.setProfilePhoto(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 6️⃣ Return updated profile
        String firstName = decrypt(user.getFirstName());
        String lastName = decrypt(user.getLastName());
        String mobile = decrypt(user.getMobile());
        String name = (firstName != null && lastName != null) ? firstName + " " + lastName : null;
        String role = user.getRole().name();

        return UserProfileResponse.builder()
                .userId(user.getId())
                .id(user.getId().toString())
                .firstName(firstName)
                .lastName(lastName)
                .name(name)
                .email(user.getEmail())
                .mobile(mobile)
                .mobileNumber(mobile)
                .phone(mobile)
                .dob(decrypt(user.getDob()))
                .profilePhoto(null)
                .photoURL(null)
                .avatar(null)
                .city(decrypt(user.getCity()))
                .state(decrypt(user.getState()))
                .country(decrypt(user.getCountry()))
                .countryCode(user.getCountryCode())
                .preferredLanguage(user.getPreferredLanguage())
                .organization(decrypt(user.getOrganization()))
                .skills(user.getSkills())
                .fieldOfStudy(user.getFieldOfStudy())
                .highestQualification(user.getHighestQualification())
                .gender(user.getGender())
                .role(role)
                .userRole(role)
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .activeSessions(null)
                .build();
    }

    public UserProfileResponse uploadCoverPhoto(HttpServletRequest request, MultipartFile file) {
        // 1️⃣ Get authenticated user ID
        UUID userId = resolveAuthenticatedUserId(request);

        // 2️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfilePhotoException(ProfilePhotoException.ErrorCode.USER_NOT_FOUND));

        // 3️⃣ Validate file
        if (file.isEmpty()) {
            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.INVALID_FILE_TYPE);
        }

        // 4️⃣ Upload to Google Drive
        String coverPhotoUrl;
        try {
            coverPhotoUrl = googleDriveService.uploadFile(file);
        } catch (Exception e) {
            log.error("Failed to upload cover photo to Google Drive: {}", e.getMessage(), e);
            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.UPLOAD_FAILED);
        }

        // 5️⃣ Update user with new cover photo
        user.setCoverPhoto(coverPhotoUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 6️⃣ Return updated profile
        String firstName = decrypt(user.getFirstName());
        String lastName = decrypt(user.getLastName());
        String mobile = decrypt(user.getMobile());
        String name = (firstName != null && lastName != null) ? firstName + " " + lastName : null;
        String role = user.getRole().name();

        return UserProfileResponse.builder()
                .userId(user.getId())
                .id(user.getId().toString())
                .firstName(firstName)
                .lastName(lastName)
                .name(name)
                .email(user.getEmail())
                .mobile(mobile)
                .mobileNumber(mobile)
                .phone(mobile)
                .dob(decrypt(user.getDob()))
                .profilePhoto(user.getProfilePhoto())
                .coverPhoto(coverPhotoUrl)
                .photoURL(user.getProfilePhoto())
                .avatar(user.getProfilePhoto())
                .city(decrypt(user.getCity()))
                .state(decrypt(user.getState()))
                .country(decrypt(user.getCountry()))
                .countryCode(user.getCountryCode())
                .preferredLanguage(user.getPreferredLanguage())
                .organization(decrypt(user.getOrganization()))
                .skills(user.getSkills())
                .fieldOfStudy(user.getFieldOfStudy())
                .highestQualification(user.getHighestQualification())
                .gender(user.getGender())
                .role(role)
                .userRole(role)
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .activeSessions(null)
                .build();
    }

    public UserProfileResponse removeCoverPhoto(HttpServletRequest request) {
        // 1️⃣ Get authenticated user ID
        UUID userId = resolveAuthenticatedUserId(request);

        // 2️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProfilePhotoException(ProfilePhotoException.ErrorCode.USER_NOT_FOUND));

        // 3️⃣ Check if user has a cover photo
        if (user.getCoverPhoto() == null || user.getCoverPhoto().isEmpty()) {
            throw new ProfilePhotoException(ProfilePhotoException.ErrorCode.NO_PHOTO_TO_REMOVE);
        }

        // 4️⃣ Delete the file from Google Drive
        try {
            String coverPhotoUrl = user.getCoverPhoto();
            String fileId = googleDriveService.extractFileId(coverPhotoUrl);
            if (fileId != null) {
                googleDriveService.deleteFile(fileId);
            }
        } catch (Exception e) {
            log.error("Failed to delete cover photo from Google Drive: {}", e.getMessage(), e);
            // Continue with database cleanup even if file deletion fails
        }

        // 5️⃣ Clear the cover photo from user record
        user.setCoverPhoto(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 6️⃣ Return updated profile
        String firstName = decrypt(user.getFirstName());
        String lastName = decrypt(user.getLastName());
        String mobile = decrypt(user.getMobile());
        String name = (firstName != null && lastName != null) ? firstName + " " + lastName : null;
        String role = user.getRole().name();

        return UserProfileResponse.builder()
                .userId(user.getId())
                .id(user.getId().toString())
                .firstName(firstName)
                .lastName(lastName)
                .name(name)
                .email(user.getEmail())
                .mobile(mobile)
                .mobileNumber(mobile)
                .phone(mobile)
                .dob(decrypt(user.getDob()))
                .profilePhoto(user.getProfilePhoto())
                .coverPhoto(null)
                .photoURL(user.getProfilePhoto())
                .avatar(user.getProfilePhoto())
                .city(decrypt(user.getCity()))
                .state(decrypt(user.getState()))
                .country(decrypt(user.getCountry()))
                .countryCode(user.getCountryCode())
                .preferredLanguage(user.getPreferredLanguage())
                .organization(decrypt(user.getOrganization()))
                .skills(user.getSkills())
                .fieldOfStudy(user.getFieldOfStudy())
                .highestQualification(user.getHighestQualification())
                .gender(user.getGender())
                .role(role)
                .userRole(role)
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .activeSessions(null)
                .build();
    }


    public ApiResponse<UserProfileResponse> softDeleteUser(HttpServletRequest request) {

        // 1️⃣ Get authenticated user ID

        UUID userId = resolveAuthenticatedUserId(request);

        // 2️⃣ Fetch user

        User user = userRepository.findById(userId)

                .orElseThrow(() -> new RuntimeException("User not found"));



        // 3️⃣ Soft delete: mark user as DELETED

        user.setStatus(User.Status.DELETED);

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // 4️⃣ Immediately blacklist the current active token from request header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String currentToken = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(currentToken);
        }

        // 5️⃣ Blacklist all sessions & tokens of this user
        sessionService.invalidateAllSessionsForUser(user);



        String firstName = decrypt(user.getFirstName());
        String lastName = decrypt(user.getLastName());
        String mobile = decrypt(user.getMobile());
        String profilePhoto = user.getProfilePhoto();
        String name = (firstName != null && lastName != null) ? firstName + " " + lastName : null;
        String role = user.getRole().name();

        UserProfileResponse profile = UserProfileResponse.builder()

                .firstName(firstName)

                .lastName(lastName)

                .name(name)

                .email(user.getEmail())

                .mobile(mobile)

                .mobileNumber(mobile)

                .phone(mobile)

                .dob(decrypt(user.getDob()))

                .profilePhoto(profilePhoto)

                .photoURL(profilePhoto)

                .avatar(profilePhoto)

                .city(decrypt(user.getCity()))

                .state(decrypt(user.getState()))

                .country(decrypt(user.getCountry()))

                .countryCode(user.getCountryCode())

                .preferredLanguage(user.getPreferredLanguage())

                .organization(decrypt(user.getOrganization()))

                .skills(user.getSkills())

                .fieldOfStudy(user.getFieldOfStudy())

                .highestQualification(user.getHighestQualification())

                .gender(user.getGender())

                .userId(user.getId())

                .id(user.getId().toString())

                .role(role)

                .userRole(role)

                .status(user.getStatus().name())

                .createdAt(user.getCreatedAt())

                .updatedAt(user.getUpdatedAt())

                .lastLogin(user.getLastLogin())

                .activeSessions(null)

                .build();



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

                        .lastLogin(user.getLastLogin())

                        .build())

                .collect(Collectors.toList());

    }

    public Map<String, Object> getAllUsersProfilesPaginated(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserProfileResponse> users = userPage.stream()
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
                        .lastLogin(user.getLastLogin())
                        .build())
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("currentPage", userPage.getNumber());
        response.put("totalPages", userPage.getTotalPages());
        response.put("totalUsers", userPage.getTotalElements());
        response.put("pageSize", userPage.getSize());
        
        return response;
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

                .lastLogin(user.getLastLogin())

                .build();

    }

    public UserProfileResponse updateUserStatus(UUID id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String enumStatus = status.toUpperCase();
            if ("INACTIVE".equals(enumStatus)) {
                enumStatus = "SUSPENDED"; // Map INACTIVE to SUSPENDED
            }
            user.setStatus(User.Status.valueOf(enumStatus));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
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