package com.example.admin.controller;

import com.example.admin.dto.*;
import com.example.admin.security.AdminPrincipal;
import com.example.admin.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthService adminAuthService;

    /**
     * Register Sub Admin for one service. Main Admin Bearer token required.
     * Body: { "email", "password", "confirmPassword", "assignedService", ... }
     */
    @PostMapping("/register")
    public ResponseEntity<AdminRegisterResponse> register(@RequestBody AdminRegisterRequest request) {
        try {
            AdminRegisterResponse response = adminAuthService.registerAdmin(request);
            HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                        .body(AdminRegisterResponse.builder()
                                .success(false)
                                .message(e.getReason())
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(AdminRegisterResponse.builder()
                                .success(false)
                                .message("Registration failed: " + e.getMessage())
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
                @RequestBody UpdateAdminProfileRequest request,
                @AuthenticationPrincipal AdminPrincipal adminPrincipal,
                HttpServletRequest httpRequest) {

        try {
            AdminProfileResponse response =
                        adminAuthService.updateProfile(adminPrincipal.getAdminId(), request, httpRequest);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Something went wrong: " + e.getMessage());

        }

    }

    @GetMapping("/me")
    public AdminProfileResponse getProfile(
                @AuthenticationPrincipal AdminPrincipal adminPrincipal,
                HttpServletRequest httpRequest) {

        return adminAuthService.getProfile(adminPrincipal.getAdminId(), httpRequest);
    }
}
