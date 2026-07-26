package com.example.notification.service;

import com.example.notification.dto.CourseNotificationRequest;
import com.example.notification.dto.CourseNotificationResponse;
import com.example.notification.entity.CourseNotification;
import com.example.notification.entity.CourseNotificationDelivery;
import com.example.notification.repository.CourseNotificationDeliveryRepository;
import com.example.notification.repository.CourseNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseNotificationService {

    private final CourseNotificationRepository notificationRepo;
    private final CourseNotificationDeliveryRepository deliveryRepo;

    // 🔥 Inject user-service (Feign / REST)
    // private final UserClient userClient;

    public CourseNotificationResponse notifyCourse(
            Long courseId,
            CourseNotificationRequest request) {

        UUID notificationId = UUID.randomUUID();

        // ✅ 1. FETCH USERS (REAL)
        List<UUID> users = resolveUsers(courseId, request);

        if (users.isEmpty()) {
            throw new RuntimeException("No users found");
        }

        // ✅ 2. CHANNELS
        List<String> channels = Optional.ofNullable(request.getChannels())
                .filter(c -> !c.isEmpty())
                .orElseThrow(() -> new RuntimeException("Channels required"));

        // ✅ 3. SAVE NOTIFICATION
        CourseNotification notification = CourseNotification.builder()
                .id(notificationId)
                .courseId(courseId)
                .title(request.getTitle())
                .message(request.getMessage())
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .channels(channels)
                .build();

        notificationRepo.save(notification);

        // ✅ 4. CREATE DELIVERY (REAL)
        List<CourseNotificationDelivery> deliveries = new ArrayList<>();

        for (UUID user : users) {
            for (String channel : channels) {

                deliveries.add(
                        CourseNotificationDelivery.builder()
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

        // ✅ 5. FETCH REAL DATA
        List<CourseNotificationDelivery> saved =
                deliveryRepo.findByNotificationId(notificationId);

        // ✅ 6. RESPONSE (REAL DATA)
        return mapToResponse(notification, saved);
    }

    // =========================
    // DYNAMIC USER RESOLUTION
    // =========================
    private List<UUID> resolveUsers(Long courseId, CourseNotificationRequest request) {

        if (Boolean.TRUE.equals(request.getSendToAll())) {

            // 🔥 Replace with real call
            // return userClient.getUsersByCourse(courseId);

            throw new RuntimeException("Connect user-service here");
        }

        return Optional.ofNullable(request.getUserIds())
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new RuntimeException("UserIds required"));
    }

    // =========================
    // RESPONSE MAPPING
    // =========================
    private CourseNotificationResponse mapToResponse(
            CourseNotification n,
            List<CourseNotificationDelivery> deliveries) {

        return CourseNotificationResponse.builder()
                .notificationId(n.getId())
                .courseId(n.getCourseId())
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
                        deliveries.stream().map(d ->
                                CourseNotificationResponse.DeliveryInfo.builder()
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