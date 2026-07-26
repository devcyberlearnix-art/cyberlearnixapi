package com.example.admin.controller;

import com.example.admin.dto.*;
import com.example.admin.security.JwtService;
import com.example.admin.service.AdminAuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final JwtService jwtService;
    
    /**
     * Admin Login endpoint
     * Body: { "email", "password" }
     */
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request, HttpServletRequest httpRequest) {
        try {
            AdminLoginResponse response = adminAuthService.login(request, httpRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AdminLoginResponse.builder()
                            .admin(null)
                            .authentication(null)
                            .sessionInfo(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdminLoginResponse.builder()
                            .admin(null)
                            .authentication(null)
                            .sessionInfo(null)
                            .build());
        }
    }
    
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
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdminRegisterResponse.builder()
                            .success(false)
                            .message("Registration failed: " + e.getMessage())
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        }
    }
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateAdminProfileRequest request,
            HttpServletRequest httpRequest) {

        try {

            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            UUID adminId = jwtService.extractAdminId(token);

            AdminProfileResponse response =
                    adminAuthService.updateProfile(adminId, request, httpRequest);

            return ResponseEntity.ok(response);

        } catch (ExpiredJwtException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Access token expired. Please login again.");

        } catch (JwtException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong");

        }

    }
    @GetMapping("/me")
    public AdminProfileResponse getProfile(HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        UUID adminId = jwtService.extractAdminId(token);

        return adminAuthService.getProfile(adminId, httpRequest);
    }
}