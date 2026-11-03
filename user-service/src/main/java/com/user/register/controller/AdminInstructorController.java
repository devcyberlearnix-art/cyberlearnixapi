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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
            if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
                try {
                    InstructorApplication.ApplicationStatus applicationStatus = InstructorApplication.ApplicationStatus.valueOf(status.toUpperCase());
                    applications = instructorService.getApplicationsByStatusPaginated(applicationStatus, page, size);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(false, "Invalid status: " + status + ". Valid values: DRAFT, PENDING, APPROVED, REJECTED, NEED_MORE_INFO", null, LocalDateTime.now()));
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
     * Get single application details
     */
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> getApplicationById(
            @PathVariable UUID applicationId) {
        try {
            InstructorApplyDetailedResponse response = instructorService.getApplicationById(applicationId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Instructor application details fetched successfully",
                    response,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch application details: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Approve instructor application by application ID
     */
    @PutMapping("/applications/{applicationId}/approve")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> approveInstructorApplication(
            @PathVariable UUID applicationId,
            @RequestHeader(value = "X-User-Authorization", required = false) String userAuthorization,
            @RequestHeader(value = "Authorization", required = false) String serviceAuthorization) {
        try {
            String token = userAuthorization != null ? userAuthorization : serviceAuthorization;
            UUID adminId = BearerTokenResolver.resolveAdminAccessToken(token, jwtUtil, userRepository);
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
     * Reject instructor application by application ID with mandatory reason
     */
    @PutMapping("/applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> rejectInstructorApplication(
            @PathVariable UUID applicationId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Authorization", required = false) String userAuthorization,
            @RequestHeader(value = "Authorization", required = false) String serviceAuthorization) {
        try {
            String token = userAuthorization != null ? userAuthorization : serviceAuthorization;
            UUID adminId = BearerTokenResolver.resolveAdminAccessToken(token, jwtUtil, userRepository);

            String rejectionReason = (body != null && body.containsKey("reason")) ? body.get("reason") : reason;
            if (rejectionReason == null || rejectionReason.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "A rejection reason is strictly mandatory.", null, LocalDateTime.now()));
            }

            InstructorApplyDetailedResponse response = instructorService.rejectApplicationById(applicationId, adminId, rejectionReason);
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

    /**
     * Request additional information from applicant
     */
    @PutMapping("/applications/{applicationId}/request-info")
    public ResponseEntity<ApiResponse<InstructorApplyDetailedResponse>> requestAdditionalInfo(
            @PathVariable UUID applicationId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(required = false) String info,
            @RequestHeader(value = "X-User-Authorization", required = false) String userAuthorization,
            @RequestHeader(value = "Authorization", required = false) String serviceAuthorization) {
        try {
            String token = userAuthorization != null ? userAuthorization : serviceAuthorization;
            UUID adminId = BearerTokenResolver.resolveAdminAccessToken(token, jwtUtil, userRepository);

            String requestedInfo = null;
            if (body != null) {
                requestedInfo = body.get("requestedInfo");
                if (requestedInfo == null || requestedInfo.isBlank()) requestedInfo = body.get("reason");
                if (requestedInfo == null || requestedInfo.isBlank()) requestedInfo = body.get("note");
                if (requestedInfo == null || requestedInfo.isBlank()) requestedInfo = body.get("message");
            }
            if (requestedInfo == null || requestedInfo.isBlank()) requestedInfo = info;
            if (requestedInfo == null || requestedInfo.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Requested info notes are required.", null, LocalDateTime.now()));
            }

            InstructorApplyDetailedResponse response = instructorService.requestInfoById(applicationId, adminId, requestedInfo);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Information request sent to applicant successfully",
                    response,
                    LocalDateTime.now()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to request info: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Admin view applicant sensitive document
     */
    @GetMapping("/applications/{applicationId}/documents/{docType}")
    public ResponseEntity<Resource> getApplicantDocument(
            @PathVariable UUID applicationId,
            @PathVariable String docType,
            @RequestHeader(value = "X-User-Authorization", required = false) String userAuthorization,
            @RequestHeader(value = "Authorization", required = false) String serviceAuthorization) {
        String token = userAuthorization != null ? userAuthorization : serviceAuthorization;
        UUID adminId = BearerTokenResolver.resolveAdminAccessToken(token, jwtUtil, userRepository);
        Resource resource = instructorService.getDocumentResource(applicationId, docType, adminId, "ROLE_MAIN_ADMIN");

        String contentType = "application/octet-stream";
        if (resource.getFilename() != null) {
            String fn = resource.getFilename().toLowerCase();
            if (fn.endsWith(".pdf")) contentType = "application/pdf";
            else if (fn.endsWith(".png")) contentType = "image/png";
            else if (fn.endsWith(".jpg") || fn.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fn.endsWith(".webp")) contentType = "image/webp";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
