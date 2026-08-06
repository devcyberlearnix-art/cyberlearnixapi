package com.example.notification.service;

import com.example.notification.dto.DeviceTokenRequest;
import com.example.notification.dto.DeviceTokenResponse;
import com.example.notification.entity.DeviceToken;
import com.example.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository repository;

    public DeviceTokenResponse saveToken(String userId, DeviceTokenRequest request) {

        // prevent duplicate token for same user
        DeviceToken token = repository
                .findByTokenAndUserId(request.getToken(), userId)
                .orElse(DeviceToken.builder()
                        .userId(userId)
                        .token(request.getToken())
                        .deviceType(request.getDeviceType())
                        .deviceId(request.getDeviceId())
                        .build());

        token.setActive(true);

        DeviceToken saved = repository.save(token);

        return DeviceTokenResponse.builder()
                .deviceTokenId(saved.getId())
                .userId(saved.getUserId())
                .token(saved.getToken())
                .deviceType(saved.getDeviceType())
                .deviceId(saved.getDeviceId())
                .active(saved.isActive())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}