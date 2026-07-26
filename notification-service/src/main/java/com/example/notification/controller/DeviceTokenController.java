package com.example.notification.controller;

import com.example.notification.dto.ApiResponse;
import com.example.notification.dto.DeviceTokenRequest;
import com.example.notification.dto.DeviceTokenResponse;
import com.example.notification.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService service;

    @PostMapping
    public ResponseEntity<?> registerDeviceToken(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody DeviceTokenRequest request
    ) {

        DeviceTokenResponse response = service.saveToken(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Device token registered successfully", response)
        );
    }
}