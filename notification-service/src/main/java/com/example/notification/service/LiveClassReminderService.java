package com.example.notification.service;

import com.example.notification.dto.LiveClassReminderRequest;
import com.example.notification.dto.LiveClassReminderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LiveClassReminderService {

    // 🔥 You can inject userService / repo later

    public LiveClassReminderResponse sendReminder(LiveClassReminderRequest request) {

        UUID reminderId = UUID.randomUUID();

        // ✅ Resolve users dynamically
        List<UUID> users = resolveUsers(request);

        // ✅ Channel dynamic (later from DB)
        String channel = "IN_APP";

        // ✅ Status dynamic
        String status = LocalDateTime.now().isBefore(request.getClassStartTime())
                ? "SCHEDULED"
                : "SENT";

        // ✅ Build delivery list
        List<LiveClassReminderResponse.DeliveryInfo> deliveries =
                users.stream().map(userId ->
                        LiveClassReminderResponse.DeliveryInfo.builder()
                                .userId(userId.toString())
                                .channel(channel)
                                .status(status.equals("SENT") ? "DELIVERED" : "QUEUED")
                                .attemptedAt(toInstant(LocalDateTime.now()))
                                .build()
                ).toList();

        return LiveClassReminderResponse.builder()
                .reminderId(reminderId)
                .courseId(request.getCourseId())
                .instructorId(request.getInstructorId())
                .title(request.getTitle())
                .message(request.getMessage())

                .scheduledAt(toInstant(request.getClassStartTime()))
                .status(status)

                .channels(List.of(channel))
                .targetUsers(users.stream().map(UUID::toString).toList())
                .deliveryStatuses(deliveries)

                .build();
    }

    // ==============================
    // HELPERS
    // ==============================

    private List<UUID> resolveUsers(LiveClassReminderRequest request) {
        if (Boolean.TRUE.equals(request.getSendToAll())) {
            return getAllUsersOfCourse(request.getCourseId());
        }
        return request.getUserIds() != null ? request.getUserIds() : Collections.emptyList();
    }

    private List<UUID> getAllUsersOfCourse(Long courseId) {
        // 🔥 Replace with DB/user-service call
        return new ArrayList<>();
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant();
    }
}