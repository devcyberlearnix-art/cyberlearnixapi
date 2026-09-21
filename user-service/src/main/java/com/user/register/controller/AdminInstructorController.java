package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.InstructorApplyDetailedResponse;
import com.user.register.dto.UserProfileResponse;
import com.user.register.entity.InstructorApplication;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.service.InstructorService;
import com.user.register.service.UserService;
import com.user.register.security.JwtUtil;
import com.user.register.util.BearerTokenResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/instructors")
@PreAuthorize("hasAnyRole('MAIN_ADMIN', 'SUB_ADMIN')")
public class AdminInstructorController {

    private final InstructorService instructorService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AdminInstructorController(InstructorService instructorService, UserService userService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.instructorService = instructorService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /**
     * Get all approved instructors
     * Admin only endpoint
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllInstructors() {
        try {
            List<User> instructors = userService.getAllInstructors();
            List<UserProfileResponse> instructorProfiles = instructors.stream()
                    .map(user -> {
                        UserProfileResponse profile = new UserProfileResponse();
                        profile.setUserId(user.getId());
                        profile.setEmail(user.getEmail());
                        profile.setRole(user.getRole().toString());
                        profile.setStatus(user.getStatus() != null ? user.getStatus().name() : "INACTIVE");
                        profile.setFirstName(user.getFirstName());
                        profile.setLastName(user.getLastName());
                        profile.setMobile(user.getMobile());
                        profile.setProfilePhoto(user.getProfilePhoto());
                        profile.setCreatedAt(user.getCreatedAt());
                        return profile;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Instructors fetched successfully",
                    instructorProfiles,
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch instructors: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Get all instructor applications with pagination and optional status filtering
     * Admin only endpoint
     * Industry standard: uses query parameters for filtering
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllInstructorApplications(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (page < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Page number must be >= 0", null, LocalDateTime.now()));
            }
            if (size < 1 || size > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Page size must be between 1 and 100", null, LocalDateTime.now()));
            }

            Map<String, Object> applications;
            if (status != null && !status.isBlank()) {
                try {
                    InstructorApplication.ApplicationStatus applicationStatus = InstructorApplication.ApplicationStatus.valueOf(status.toUpperCase());
                    applications = instructorService.getApplicationsByStatusPaginated(applicationStatus, page, size);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(false, "Invalid status: " + status + ". Valid values: PENDING, APPROVED, REJECTED", null, LocalDateTime.now()));
                }
            } else {
                applications = instructorService.getAllApplicationsPaginated(page, size);
            }

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Instructor applications fetched successfully",
                    applications,
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch applications: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Deprecated: Use GET /applications?status={status} instead
     * This endpoint is kept for backward compatibility
     *
     * @deprecated Use query parameter version: GET /applications?status={status}
     */
    @Deprecated
    @GetMapping("/applications/status/{status}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInstructorApplicationsByStatusLegacy(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Redirect to the new query parameter implementation
        return getAllInstructorApplications(status, page, size);
    }

    /**
     * Approve instructor application by application ID
     * Admin only endpoint
     */
    @PutMapping("/applications/{applicationId}/approve")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> approveInstructorApplication(
            @PathVariable UUID applicationId,
            @RequestHeader(value = "X-User-Authorization", required = false) String userAuthorization,
            @RequestHeader(value = "Authorization", required = false) String serviceAuthorization) {
        try {
            System.out.println("X-User-Authorization header: " + userAuthorization);
            System.out.println("Authorization header: " + serviceAuthorization);
            // Admin user token from X-User-Authorization for business authorization
            UUID adminId = BearerTokenResolver.resolveAdminAccessToken(userAuthorization, jwtUtil, userRepository);
            InstructorApplyDetailedResponse response = instructorService.approveApplicationById(applicationId, adminId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Instructor application approved successfully",
                    response,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to approve application: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Reject instructor application by application ID
     * Admin only endpoint
     */
    @PutMapping("/applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> rejectInstructorApplication(
            @PathVariable UUID applicationId,
            @RequestHeader(value = "X-User-Authorization", required = false) String userAuthorization,
            @RequestHeader(value = "Authorization", required = false) String serviceAuthorization) {
        try {
            System.out.println("X-User-Authorization header: " + userAuthorization);
            System.out.println("Authorization header: " + serviceAuthorization);
            // Admin user token from X-User-Authorization for business authorization
            UUID adminId = BearerTokenResolver.resolveAdminAccessToken(userAuthorization, jwtUtil, userRepository);
            InstructorApplyDetailedResponse response = instructorService.rejectApplicationById(applicationId, adminId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Instructor application rejected successfully",
                    response,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to reject application: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }
}
