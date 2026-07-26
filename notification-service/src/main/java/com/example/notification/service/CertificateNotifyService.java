package com.example.notification.service;

import com.example.notification.dto.*;
import com.example.notification.entity.CertificateDelivery;
import com.example.notification.entity.CertificateNotification;
import com.example.notification.repository.CertificateDeliveryRepository;
import com.example.notification.repository.CertificateNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
@Service
@RequiredArgsConstructor
public class CertificateNotifyService {

    private final CertificateNotificationRepository notificationRepo;
    private final CertificateDeliveryRepository deliveryRepo;

    // 🔥 Replace with Feign / REST client
    // private final UserClient userClient;

    public CertificateNotifyResponse notifyUsers(CertificateNotifyRequest request) {

        UUID notificationId = UUID.randomUUID();

        // ✅ 1. FETCH USERS (REAL)
        List<UUID> users = resolveUsers(request);

        if (users.isEmpty()) {
            throw new RuntimeException("No users found for notification");
        }

        // ✅ 2. CHANNELS (FROM REQUEST)
        List<String> channels = Optional.ofNullable(request.getChannels())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new RuntimeException("Channels are required"));

        // ✅ 3. STATUS
        String status = resolveStatus(request.getIssuedAt());

        // ✅ 4. SAVE NOTIFICATION
        CertificateNotification notification = CertificateNotification.builder()
                .id(notificationId)
                .courseId(request.getCourseId())
                .certificateId(request.getCertificateId())
                .instructorId(request.getInstructorId())
                .title(request.getTitle())
                .message(request.getMessage())
                .issuedAt(request.getIssuedAt())
                .status(status)
                .channels(channels)
                .build();

        notificationRepo.save(notification);

        // ✅ 5. CREATE DELIVERY (REAL)
        List<CertificateDelivery> deliveries = new ArrayList<>();

        for (UUID user : users) {
            for (String channel : channels) {

                CertificateDelivery delivery = CertificateDelivery.builder()
                        .id(UUID.randomUUID())
                        .notificationId(notificationId)
                        .userId(user)
                        .channel(channel)
                        .status(resolveDeliveryStatus(status))
                        .attemptedAt(LocalDateTime.now())
                        .build();

                deliveries.add(delivery);
            }
        }

        deliveryRepo.saveAll(deliveries);

        // ✅ 6. FETCH SAVED DELIVERY (REAL DATA)
        List<CertificateDelivery> savedDeliveries =
                deliveryRepo.findByNotificationId(notificationId);

        // ✅ 7. BUILD RESPONSE
        return mapToResponse(notification, savedDeliveries);
    }

    // =========================================
    // 🔥 REAL USER RESOLUTION
    // =========================================
    private List<UUID> resolveUsers(CertificateNotifyRequest request) {

        if (Boolean.TRUE.equals(request.getSendToAll())) {

            // 🔥 Replace with real API call
            // return userClient.getUsersByCourse(request.getCourseId());

            throw new RuntimeException("Implement user-service call here");
        }

        return Optional.ofNullable(request.getUserIds())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new RuntimeException("UserIds required when sendToAll=false"));
    }

    private String resolveStatus(LocalDateTime issuedAt) {

        if (issuedAt == null) return "CREATED";

        return LocalDateTime.now().isBefore(issuedAt)
                ? "SCHEDULED"
                : "SENT";
    }

    private String resolveDeliveryStatus(String status) {
        return switch (status) {
            case "SCHEDULED" -> "QUEUED";
            case "SENT" -> "DELIVERED";
            default -> "QUEUED";
        };
    }

    // =========================================
    // 🔥 RESPONSE MAPPING (REAL DATA)
    // =========================================
    private CertificateNotifyResponse mapToResponse(
            CertificateNotification n,
            List<CertificateDelivery> deliveries) {

        return CertificateNotifyResponse.builder()
                .notificationId(n.getId())
                .courseId(n.getCourseId())
                .certificateId(n.getCertificateId())
                .instructorId(n.getInstructorId())
                .title(n.getTitle())
                .message(n.getMessage())
                .issuedAt(toInstant(n.getIssuedAt()))
                .status(n.getStatus())

                .channels(n.getChannels())

                .targetUsers(
                        deliveries.stream()
                                .map(d -> d.getUserId().toString())
                                .distinct()
                                .toList()
                )

                .deliveryStatuses(
                        deliveries.stream().map(d ->
                                CertificateNotifyResponse.DeliveryInfo.builder()
                                        .userId(d.getUserId().toString())
                                        .channel(d.getChannel())
                                        .status(d.getStatus())
                                        .attemptedAt(toInstant(d.getAttemptedAt()))
                                        .build()
                        ).toList()
                )
                .build();
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant();
    }
}