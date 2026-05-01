package com.user.register.service;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.SessionDto;
import com.user.register.dto.UpdateUserProfileRequest;
import com.user.register.dto.UserProfileResponse;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    private final String encryptionKey = "1234567890123456";

    public UserService(UserRepository userRepository,
                       UserSessionRepository sessionRepository,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Helper to get the Authenticated User ID from SecurityContext
     */
    private Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        return Long.parseLong(principal.toString());
    }

    public UserProfileResponse getLoggedInUserProfile(HttpServletRequest request) {
        Long userId = getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildProfileResponse(user, true);
    }

    public UserProfileResponse updateUserProfile(HttpServletRequest request, UpdateUserProfileRequest updateRequest) {
        Long userId = getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
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
            throw new RuntimeException("Error encrypting user fields", e);
        }

        if (updateRequest.getPreferredLanguage() != null) user.setPreferredLanguage(updateRequest.getPreferredLanguage());
        if (updateRequest.getProfilePhoto() != null) user.setProfilePhoto(updateRequest.getProfilePhoto());
        if (updateRequest.getSkills() != null) user.setSkills(updateRequest.getSkills());
        if (updateRequest.getFieldOfStudy() != null) user.setFieldOfStudy(updateRequest.getFieldOfStudy());
        if (updateRequest.getHighestQualification() != null) user.setHighestQualification(updateRequest.getHighestQualification());

        userRepository.save(user);
        return buildProfileResponse(user, false);
    }

    public UserProfileResponse uploadProfilePhoto(HttpServletRequest request, MultipartFile file) {
        Long userId = getAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (file.isEmpty()) throw new RuntimeException("No file uploaded");

        try {
            String filename = processAndSaveImage(file);
            user.setProfilePhoto("/uploads/" + filename);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return buildProfileResponse(user, false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }

    public ApiResponse<UserProfileResponse> softDeleteUser(HttpServletRequest request) {
        Long userId = getAuthenticatedUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(User.Status.SUSPENDED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return new ApiResponse<>(true, "Account deactivated", buildProfileResponse(user, false), LocalDateTime.now());
    }

    // Restored socialLogin method
    public User socialLogin(String email, String provider) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new User();
            user.setEmail(email);
            user.setStatus(User.Status.PENDING_VERIFICATION);
            user.setRole(User.Role.STUDENT);
            user.setProvider(provider);
            userRepository.save(user);
        }
        return user;
    }

    // --- Helper Methods ---

    private String decrypt(String value) {
        if (value == null) return null;
        try {
            return SecurityUtils.decrypt(value, encryptionKey);
        } catch (Exception e) {
            return value;
        }
    }

    private UserProfileResponse buildProfileResponse(User user, boolean includeSessions) {
        List<SessionDto> activeSessions = null;
        if (includeSessions) {
            activeSessions = sessionRepository.findByUser(user).stream()
                    .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                    .map(s -> new SessionDto(s.getId(), user.getId(), s.getDeviceInfo(), s.getIpAddress(), s.getCreatedAt(), user.getEmail()))
                    .collect(Collectors.toList());
        }

        return new UserProfileResponse(
                user.getId(), decrypt(user.getFirstName()), decrypt(user.getLastName()), user.getEmail(),
                decrypt(user.getMobile()), decrypt(user.getDob()), user.getProfilePhoto(),
                decrypt(user.getCity()), decrypt(user.getState()), decrypt(user.getCountry()),
                user.getPreferredLanguage(), decrypt(user.getOrganization()), user.getSkills(),
                user.getFieldOfStudy(), user.getHighestQualification(), user.getRole().name(),
                user.getStatus().name(), user.getCreatedAt(), user.getUpdatedAt(), user.getLastLogin(), activeSessions
        );
    }

    private String processAndSaveImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        BufferedImage resizedImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, 512, 512, null);
        g.dispose();

        String extension = "png";
        String filename = UUID.randomUUID().toString() + "." + extension;
        String uploadDir = "uploads";
        Files.createDirectories(Paths.get(uploadDir));
        ImageIO.write(resizedImage, extension, new File(uploadDir + File.separator + filename));
        return filename;
    }
}