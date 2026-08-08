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
    public ResponseEntity<AdminUsersResponse> getAllUsers() {
        try {
            AdminUsersResponse response = adminUserService.getAllUsers();
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
    public ResponseEntity<AdminUsersResponse> getAllInstructors() {

        AdminUsersResponse response = adminUserService.getAllInstructors();

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 400)
                .body(response);
    }

    @GetMapping("/instructors/applications")
    public ResponseEntity<AdminInstructorApplicationsResponse> getAllInstructorApplicationsDetailed() {
        AdminInstructorApplicationsResponse response = adminUserService.getAllInstructorApplicationsDetailed();
        return ResponseEntity
                .status(response.isSuccess() ? 200 : 500)
                .body(response);
    }

    @PutMapping("/instructors/applications/{userId}/approve")
    public ResponseEntity<AdminApproveInstructorResponse> approveInstructorApplicationByUserId(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authorization) {
        AdminApproveInstructorResponse response = adminUserService.approveInstructorApplicationByUserId(userId, authorization);
        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PutMapping("/instructors/applications/{userId}/reject")
    public ResponseEntity<AdminApproveInstructorResponse> rejectInstructorApplicationByUserId(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authorization) {
        AdminApproveInstructorResponse response = adminUserService.rejectInstructorApplicationByUserId(userId, authorization);
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