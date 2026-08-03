package com.example.notification.controller;

import com.example.notification.dto.ApiResponse;
import com.example.notification.dto.SystemHealthResponse;
import com.example.notification.service.SystemHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemHealthService healthService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> health() {

        SystemHealthResponse response = healthService.getHealthStatus();

        // ❌ If any service is DOWN → 503
        if ("DOWN".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.<SystemHealthResponse>builder()
                            .requestId(UUID.randomUUID().toString())
                            .timestamp(LocalDateTime.now())
                            .status("ERROR")
                            .message("Service is DOWN")
                            .data(response)   // 🔥 include detailed checks
                            .build());
        }

        // ✅ All good → 200
        return ResponseEntity.ok(
                ApiResponse.success("Service is healthy", response)
        );
    }
}