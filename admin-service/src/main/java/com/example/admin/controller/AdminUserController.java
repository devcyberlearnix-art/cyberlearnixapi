package com.example.admin.controller;

import com.example.admin.dto.AdminApproveInstructorResponse;
import com.example.admin.dto.AdminDeleteUserResponse;
import com.example.admin.dto.AdminInstructorApplicationsResponse;
import com.example.admin.dto.AdminSingleUserResponse;
import com.example.admin.dto.AdminUsersResponse;
import com.example.admin.dto.UpdateUserStatusRequest;
import com.example.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ResponseEntity<AdminUsersResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            AdminUsersResponse response = adminUserService.getAllUsers(page, size);
            return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
        } catch (Exception e) {
            System.err.println("Error in getAllUsers: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                AdminUsersResponse.builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build()
            );
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminSingleUserResponse> getUserById(@PathVariable UUID id) {

        AdminSingleUserResponse response = adminUserService.getUserById(id);

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    // ✅ NEW API: Activate / Deactivate user
    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminSingleUserResponse> updateUserStatus(
            @PathVariable UUID id,
            @RequestBody UpdateUserStatusRequest request) {

        AdminSingleUserResponse response = adminUserService.updateUserStatus(id, request);

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 400)
                .body(response);
    }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<AdminDeleteUserResponse> deleteUser(@PathVariable UUID id) {

        AdminDeleteUserResponse response = adminUserService.deleteUser(id);

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @GetMapping("/instructors")
    public ResponseEntity<AdminUsersResponse> getAllInstructors(
            @RequestHeader("Authorization") String authorization) {

        AdminUsersResponse response = adminUserService.getAllInstructors(authorization);

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 400)
                .body(response);
    }

    @GetMapping("/instructors/{id}")
    public ResponseEntity<com.example.admin.dto.AdminInstructorDetailResponse> getInstructorById(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        com.example.admin.dto.AdminInstructorDetailResponse response =
                adminUserService.getInstructorDetailedById(id, authorization);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .body(response);
    }

    @GetMapping("/instructors/applications")
    public ResponseEntity<AdminInstructorApplicationsResponse> getAllInstructorApplicationsDetailed(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) {
            return ResponseEntity.badRequest().body(
                AdminInstructorApplicationsResponse.builder()
                    .success(false)
                    .message("Page number must be >= 0")
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build()
            );
        }
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest().body(
                AdminInstructorApplicationsResponse.builder()
                    .success(false)
                    .message("Page size must be between 1 and 100")
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build()
            );
        }

        AdminInstructorApplicationsResponse response;
        if (status != null && !status.isBlank()) {
            response = adminUserService.getInstructorApplicationsByStatusPaginated(authorization, status, page, size);
        } else {
            response = adminUserService.getAllInstructorApplicationsPaginated(authorization, page, size);
        }

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 500)
                .body(response);
    }

    /**
     * Deprecated: Use GET /instructors/applications?status={status} instead
     * This endpoint is kept for backward compatibility
     *
     * @deprecated Use query parameter version: GET /instructors/applications?status={status}
     */
    @Deprecated
    @GetMapping("/instructors/applications/status/{status}")
    public ResponseEntity<AdminInstructorApplicationsResponse> getInstructorApplicationsByStatusLegacy(
            @PathVariable String status,
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Redirect to the new query parameter implementation
        return getAllInstructorApplicationsDetailed(authorization, status, page, size);
    }

    @PutMapping("/instructors/applications/{applicationId}/approve")
    public ResponseEntity<AdminApproveInstructorResponse> approveInstructorApplicationByApplicationId(
            @PathVariable UUID applicationId,
            @RequestHeader("Authorization") String authorization) {
        AdminApproveInstructorResponse response = adminUserService.approveInstructorApplicationByApplicationId(applicationId, authorization);
        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PutMapping("/instructors/applications/{applicationId}/reject")
    public ResponseEntity<AdminApproveInstructorResponse> rejectInstructorApplicationByApplicationId(
            @PathVariable UUID applicationId,
            @RequestHeader("Authorization") String authorization) {
        AdminApproveInstructorResponse response = adminUserService.rejectInstructorApplicationByApplicationId(applicationId, authorization);
        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @DeleteMapping("/instructors/{id}")
    public ResponseEntity<AdminDeleteUserResponse> deleteInstructor(@PathVariable UUID id) {

        AdminDeleteUserResponse response = adminUserService.deleteInstructor(id);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
