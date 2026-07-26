package com.example.notification.service;

import com.example.notification.dto.*;
import com.example.notification.entity.*;
import com.example.notification.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InstructorNotificationService {

    private final InstructorNotificationRepository notificationRepo;
    private final InstructorNotificationDeliveryRepository deliveryRepo;
    private final RestTemplate restTemplate;

    public InstructorNotificationResponse notifyInstructor(
            UUID instructorId,
            InstructorNotificationRequest request) {

        UUID notificationId = UUID.randomUUID();

        // ✅ 1. RESOLVE USERS
        List<UUID> users = resolveUsers(instructorId, request);

        // ✅ HANDLE EMPTY USERS (NO 500 ERROR)
        if (users.isEmpty()) {
            return InstructorNotificationResponse.builder()
                    .notificationId(notificationId)
                    .instructorId(instructorId)
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .status("FAILED")
                    .channels(request.getChannels())
                    .targetUsers(Collections.emptyList())
                    .deliveryStatuses(Collections.emptyList())
                    .build();
        }

        // ✅ 2. VALIDATE CHANNELS
        List<String> channels = Optional.ofNullable(request.getChannels())
                .filter(c -> !c.isEmpty())
                .orElseThrow(() -> new RuntimeException("Channels required"));

        // ✅ 3. SAVE NOTIFICATION
        InstructorNotification notification = InstructorNotification.builder()
                .id(notificationId)
                .instructorId(instructorId)
                .title(request.getTitle())
                .message(request.getMessage())
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .channels(channels)
                .build();

        notificationRepo.save(notification);

        // ✅ 4. CREATE DELIVERY
        List<InstructorNotificationDelivery> deliveries = new ArrayList<>();

        for (UUID user : users) {
            for (String channel : channels) {
                deliveries.add(
                        InstructorNotificationDelivery.builder()
                                .id(UUID.randomUUID())
                                .notificationId(notificationId)
                                .userId(user)
                                .channel(channel)
                                .status("DELIVERED")
                                .attemptedAt(LocalDateTime.now())
                                .build()
                );
            }
        }

        deliveryRepo.saveAll(deliveries);

        // ✅ 5. FETCH DELIVERY DATA
        List<InstructorNotificationDelivery> saved =
                Optional.ofNullable(deliveryRepo.findByNotificationId(notificationId))
                        .orElse(Collections.emptyList());

        return mapToResponse(notification, saved);
    }

    // =========================
    // 🔥 RESOLVE USERS (SAFE)
    // =========================
    private List<UUID> resolveUsers(UUID instructorId, InstructorNotificationRequest request) {

        if (Boolean.TRUE.equals(request.getSendToAll())) {

            String url = "http://localhost:8091/api/v1/users/instructor/" + instructorId + "/students";

            String token = null;
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();

                if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
                    token = jwtAuth.getToken().getTokenValue();
                }
            } catch (Exception ignored) {}

            HttpHeaders headers = new HttpHeaders();
            if (token != null) {
                headers.setBearerAuth(token);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<List<String>> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                new ParameterizedTypeReference<List<String>>() {}
                        );

                List<String> ids = Optional.ofNullable(response.getBody())
                        .orElse(Collections.emptyList());

                return ids.stream()
                        .map(id -> {
                            try {
                                return UUID.fromString(id);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();

            } catch (Exception e) {
                // 🔥 DO NOT CRASH SERVICE
                return Collections.emptyList();
            }
        }

        return Optional.ofNullable(request.getUserIds())
                .filter(list -> !list.isEmpty())
                .orElse(Collections.emptyList());
    }

    // =========================
    // RESPONSE MAPPING
    // =========================
    private InstructorNotificationResponse mapToResponse(
            InstructorNotification n,
            List<InstructorNotificationDelivery> deliveries) {

        return InstructorNotificationResponse.builder()
                .notificationId(n.getId())
                .instructorId(n.getInstructorId())
                .title(n.getTitle())
                .message(n.getMessage())
                .status(n.getStatus())
                .channels(n.getChannels())

                .targetUsers(
                        deliveries.stream()
                                .map(d -> d.getUserId().toString())
                                .distinct()
                                .toList()
                )

                .deliveryStatuses(
                        deliveries.stream()
                                .map(d ->
                                        InstructorNotificationResponse.DeliveryInfo.builder()
                                                .userId(d.getUserId().toString())
                                                .channel(d.getChannel())
                                                .status(d.getStatus())
                                                .attemptedAt(
                                                        d.getAttemptedAt()
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant()
                                                )
                                                .build()
                                ).toList()
                )
                .build();
    }
}