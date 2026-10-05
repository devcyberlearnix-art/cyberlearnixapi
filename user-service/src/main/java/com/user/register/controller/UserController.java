package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.UpdateUserProfileRequest;
import com.user.register.dto.UserProfileResponse;
import com.user.register.entity.User;
import com.user.register.service.UserService;
import com.user.register.security.UnifiedJwtService;
import com.cyberlearnix.security.ServiceAuthUtil;
import com.user.register.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UnifiedJwtService unifiedJwtService;
    private final ServiceAuthUtil serviceAuthUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public UserController(UserService userService, UnifiedJwtService unifiedJwtService,
            ServiceAuthUtil serviceAuthUtil, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.unifiedJwtService = unifiedJwtService;
        this.serviceAuthUtil = serviceAuthUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User statistics fetched successfully",
                userService.getUserStats(),
                LocalDateTime.now()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(HttpServletRequest request) {
        try {
            UserProfileResponse profile = userService.getLoggedInUserProfile(request);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User profile fetched successfully", profile, LocalDateTime.now())
            );
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Access token expired. Please refresh your token.", null, LocalDateTime.now()));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Invalid token: " + e.getMessage(), null, LocalDateTime.now()));
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Authentication failed";
            if (msg.contains("Missing authentication") || msg.contains("Authorization")) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse<>(false, msg, null, LocalDateTime.now()));
            } else if (msg.contains("deleted") || msg.contains("suspended")) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse<>(false, msg, null, LocalDateTime.now()));
            } else {
                return ResponseEntity.status(401)
                        .body(new ApiResponse<>(false, msg, null, LocalDateTime.now()));
            }
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            HttpServletRequest request,
            @RequestBody UpdateUserProfileRequest updateRequest
    ) {
        try {
            UserProfileResponse updatedProfile = userService.updateUserProfile(request, updateRequest);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Profile updated successfully", updatedProfile, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PutMapping("/me/photo")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfilePhoto(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            UserProfileResponse updatedProfile = userService.uploadProfilePhoto(request, file);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Profile photo updated successfully", updatedProfile, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping("/public/upload-photo")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadPhotoPublic(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String photoUrl = userService.uploadPhotoPublic(file);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Photo uploaded successfully", Map.of("url", photoUrl), LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> deleteAccount(HttpServletRequest request) {
        try {
            validateSelfDeleteIdentity(request);
            ApiResponse<UserProfileResponse> response = userService.softDeleteUser(request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Account deletion failed", null, LocalDateTime.now()));
        }
    }

    private void validateSelfDeleteIdentity(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : null;
        if ((token == null || token.isBlank()) && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null || token.isBlank() || !unifiedJwtService.validateToken(token)
                || unifiedJwtService.isTokenExpired(token)
                || !"access".equals(unifiedJwtService.extractClaims(token).get("type", String.class))
                || tokenBlacklistService.isBlacklisted(token)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Valid access token required");
        }

        String tokenUserId = unifiedJwtService.extractUserId(token);
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof String principal
                && !tokenUserId.equals(principal)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Authenticated user does not match token subject");
        }
    }

    // Admin endpoint to fetch all users
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Check for service-to-service authentication
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // Allow service tokens for admin operations
                if (isServiceToken(token)) {
                    Map<String, Object> usersData = userService.getAllUsersProfilesPaginated(page, size);
                    return ResponseEntity.ok(
                            new ApiResponse<>(true, "All users fetched successfully", usersData, LocalDateTime.now())
                    );
                }
            } else {
                // No authorization header provided
                return ResponseEntity.status(401)
                        .body(new ApiResponse<>(false, "Authorization header required", null, LocalDateTime.now()));
            }
            // Regular admin authentication - should have proper JWT validation
            // For now, returning unauthorized if not a service token
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Valid authorization token required", null, LocalDateTime.now()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Authentication failed: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    // Admin endpoint to fetch user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable UUID id) {
        try {
            UserProfileResponse user = userService.getUserById(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User fetched successfully", user, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    // Admin endpoint to update user status
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            UserProfileResponse user = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User status updated successfully", user, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    // Admin endpoint to delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        try {
            authorizeAdministrativeDeletion(request);
            userService.deleteUserById(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User deleted successfully", null, LocalDateTime.now())
            );
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(409)
                    .body(new ApiResponse<>(false, "User deletion conflicts with related data", null, LocalDateTime.now()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "User deletion failed", null, LocalDateTime.now()));
        }
    }

    private void authorizeAdministrativeDeletion(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String serviceToken = request.getHeader(serviceAuthUtil.getAuthHeaderName());
        if (authorization == null || !authorization.startsWith("Bearer ")
                || !serviceAuthUtil.validateServiceToken(serviceToken)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Administrative service authentication required");
        }

        String jwt = authorization.substring(7);
        if (!unifiedJwtService.validateToken(jwt) || unifiedJwtService.isTokenExpired(jwt)
            || !"access".equals(unifiedJwtService.extractClaims(jwt).get("type", String.class))
            || tokenBlacklistService.isBlacklisted(jwt)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid administrative access token");
        }

        String role = unifiedJwtService.extractRole(jwt);
        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        if (!"MAIN_ADMIN".equals(role) && !"SUB_ADMIN".equals(role)
                && !"SUPER_ADMIN".equals(role) && !"ADMIN".equals(role)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Admin role required");
        }
    }

    // Helper method to check if token is a service token
    private boolean isServiceToken(String token) {
        try {
            // Use UnifiedJwtService to validate the token signature properly
            return unifiedJwtService.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}
