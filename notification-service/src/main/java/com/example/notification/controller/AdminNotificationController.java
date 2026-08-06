package com.example.notification.controller;

import com.example.notification.dto.*;
import com.example.notification.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService service;

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@RequestBody BroadcastRequest request) {

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

        Object result = service.updateNotificationSettings(request);

        return ResponseEntity.ok(
                ApiResponse.success("Settings updated successfully", result)
        );
    }
}