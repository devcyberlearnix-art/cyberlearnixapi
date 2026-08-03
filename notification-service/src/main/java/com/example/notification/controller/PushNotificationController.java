package com.example.notification.controller;

import com.example.notification.dto.ApiResponse;
import com.example.notification.dto.PushNotificationRequest;
import com.example.notification.dto.PushNotificationResponse;
import com.example.notification.repository.DeviceTokenRepository;
import com.example.notification.service.FirebasePushService;
import com.google.firebase.messaging.BatchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushNotificationController {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationController.class);

    private final FirebasePushService firebasePushService;
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Send push notification to specific users by their user IDs.
     * Looks up registered FCM tokens from the database.
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<PushNotificationResponse>> sendToUsers(
            @Valid @RequestBody PushNotificationRequest request) {

        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.success("userIds are required for /send endpoint", null)
            );
        }

        // Look up active FCM tokens for the given user IDs
        List<String> fcmTokens = deviceTokenRepository.findTokensByUserIds(request.getUserIds());

        if (fcmTokens.isEmpty()) {
            log.warn("⚠️ No active device tokens found for userIds: {}", request.getUserIds());
            PushNotificationResponse response = PushNotificationResponse.builder()
                    .successCount(0)
                    .failureCount(0)
                    .status("NO_TOKENS_FOUND")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("No active device tokens found for the given users", response));
        }

        BatchResponse batchResponse = firebasePushService.sendToMultipleDevices(
                fcmTokens, request.getTitle(), request.getBody(), request.getData());

        PushNotificationResponse response = PushNotificationResponse.builder()
                .successCount(batchResponse.getSuccessCount())
                .failureCount(batchResponse.getFailureCount())
                .status(batchResponse.getFailureCount() == 0 ? "SUCCESS" : "PARTIAL_FAILURE")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Push notifications sent successfully", response));
    }

    /**
     * Send push notification to a Firebase topic.
     */
    @PostMapping("/topic")
    public ResponseEntity<ApiResponse<PushNotificationResponse>> sendToTopic(
            @Valid @RequestBody PushNotificationRequest request) {

        if (request.getTopic() == null || request.getTopic().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.success("topic is required for /topic endpoint", null)
            );
        }

        String messageId = firebasePushService.sendToTopic(
                request.getTopic(), request.getTitle(), request.getBody(), request.getData());

        PushNotificationResponse response = PushNotificationResponse.builder()
                .successCount(1)
                .failureCount(0)
                .messageId(messageId)
                .topic(request.getTopic())
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Push notification sent to topic: " + request.getTopic(), response));
    }

    /**
     * Broadcast push notification to ALL active device tokens.
     */
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<PushNotificationResponse>> broadcast(
            @Valid @RequestBody PushNotificationRequest request) {

        List<String> allTokens = deviceTokenRepository.findAllActiveTokens();

        if (allTokens.isEmpty()) {
            log.warn("⚠️ No active device tokens found for broadcast");
            PushNotificationResponse response = PushNotificationResponse.builder()
                    .successCount(0)
                    .failureCount(0)
                    .status("NO_TOKENS_FOUND")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("No active device tokens found", response));
        }

        BatchResponse batchResponse = firebasePushService.sendToMultipleDevices(
                allTokens, request.getTitle(), request.getBody(), request.getData());

        PushNotificationResponse response = PushNotificationResponse.builder()
                .successCount(batchResponse.getSuccessCount())
                .failureCount(batchResponse.getFailureCount())
                .status(batchResponse.getFailureCount() == 0 ? "SUCCESS" : "PARTIAL_FAILURE")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Broadcast push sent to " + allTokens.size() + " devices", response));
    }
}
