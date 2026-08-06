package com.example.notification.controller;

import com.example.notification.dto.*;
import com.example.notification.service.InstructorNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class InstructorNotificationController {

    private final InstructorNotificationService service;
    @PostMapping("/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<InstructorNotificationResponse>> notifyInstructor(
            @PathVariable UUID instructorId,
            @RequestBody InstructorNotificationRequest request) {

        InstructorNotificationResponse response =
                service.notifyInstructor(instructorId, request);

        // ❌ No users found → 404
        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No users found for this instructor"));
        }

        // ✅ Success → 201 CREATED
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Instructor notification sent successfully",
                        response
                ));
    }
}