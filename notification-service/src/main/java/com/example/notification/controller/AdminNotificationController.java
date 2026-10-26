package com.example.notification.controller;

import com.example.notification.dto.*;
import com.example.notification.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MAIN_ADMIN', 'SUB_ADMIN')")
public class AdminNotificationController {

    private final AdminNotificationService service;

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@jakarta.validation.Valid @RequestBody BroadcastRequest request) {

        String result = service.sendBroadcast(request);

        return ResponseEntity.ok(
                ApiResponse.success("Broadcast sent successfully", result)
        );
    }

    @PostMapping("/reprocess-dlq")
    public ResponseEntity<DlqReprocessResponse> reprocessDlq() {

        DlqReprocessResponse response = service.reprocessDlq();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-health")
    public ResponseEntity<SystemHealthResponse> getSystemHealth() {
        return ResponseEntity.ok(service.getSystemHealth()); // ✅ FIXED
    }
    @PutMapping("/settings/notifications")
    public ResponseEntity<?> updateSettings(@RequestBody NotificationSettingsRequest request) {
        if (request == null || (request.getEmailEnabled() == null && request.getPushEnabled() == null && request.getSmsEnabled() == null)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Notification settings payload cannot be empty");
        }

        Object result = service.updateNotificationSettings(request);

        return ResponseEntity.ok(
                ApiResponse.success("Settings updated successfully", result)
        );
    }
}
