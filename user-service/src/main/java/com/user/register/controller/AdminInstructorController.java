package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.InstructorApplyDetailedResponse;
import com.user.register.dto.UserProfileResponse;
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
     * Get all instructor applications
     * Admin only endpoint
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<InstructorApplyDetailedResponse>>> getAllInstructorApplications() {
        try {
            List<InstructorApplyDetailedResponse> applications = instructorService.getAllApplications();
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
     * Approve instructor application by user ID
     * Admin only endpoint
     */
    @PutMapping("/applications/{userId}/approve")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> approveInstructorApplication(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authorization) {
        try {
            UUID adminId = BearerTokenResolver.resolveAdminAccessToken(authorization, jwtUtil, userRepository);
            InstructorApplyDetailedResponse response = instructorService.approveApplicationByUserId(userId, adminId);
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
     * Reject instructor application by user ID
     * Admin only endpoint
     */
    @PutMapping("/applications/{userId}/reject")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> rejectInstructorApplication(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authorization) {
        try {
            UUID adminId = BearerTokenResolver.resolveAdminAccessToken(authorization, jwtUtil, userRepository);
            InstructorApplyDetailedResponse response = instructorService.rejectApplicationByUserId(userId, adminId);
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