package com.example.notification.service;

import com.example.notification.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AssignmentReminderService {

    public AssignmentReminderResponse sendReminder(AssignmentReminderRequest request) {

        UUID reminderId = UUID.randomUUID();

        // ✅ dynamic users
        List<UUID> users = resolveUsers(request);

        // ✅ dynamic channels
        List<String> channels = resolveChannels(request);

        // ✅ dynamic status
        String status = resolveStatus(request.getDueDate());

        // ✅ build delivery dynamically
        List<AssignmentReminderResponse.DeliveryInfo> deliveries =
                users.stream()
                        .flatMap(user ->
                                channels.stream().map(channel ->
                                        buildDelivery(user, channel, status, request)
                                )
                        ).toList();

        return AssignmentReminderResponse.builder()
                .reminderId(reminderId)
                .courseId(request.getCourseId())
                .assignmentId(request.getAssignmentId())
                .instructorId(request.getInstructorId())
                .title(request.getTitle())
                .message(request.getMessage())

                .dueAt(toInstant(request.getDueDate()))
                .status(status)

                .channels(channels)
                .targetUsers(users.stream().map(UUID::toString).toList())
                .deliveryStatuses(deliveries)

                .build();
    }

    // =========================
    // HELPERS (DYNAMIC LOGIC)
    // =========================

    private List<UUID> resolveUsers(AssignmentReminderRequest request) {

        if (Boolean.TRUE.equals(request.getSendToAll())) {
            return getUsersByCourse(request.getCourseId());
        }

        return request.getUserIds() != null
                ? request.getUserIds()
                : Collections.emptyList();
    }

    private List<String> resolveChannels(AssignmentReminderRequest request) {

        if (request.getChannels() != null && !request.getChannels().isEmpty()) {
            return request.getChannels();
        }

        return List.of("IN_APP"); // fallback
    }

    private String resolveStatus(LocalDateTime dueDate) {

        if (dueDate == null) return "CREATED";

        if (LocalDateTime.now().isBefore(dueDate)) return "SCHEDULED";

        return "SENT";
    }

    private AssignmentReminderResponse.DeliveryInfo buildDelivery(
            UUID user,
            String channel,
            String status,
            AssignmentReminderRequest request) {

        return AssignmentReminderResponse.DeliveryInfo.builder()
                .userId(user.toString())
                .channel(channel)
                .status(mapDeliveryStatus(status))
                .attemptedAt(resolveAttemptTime(request))
                .build();
    }

    private String mapDeliveryStatus(String status) {
        return switch (status) {
            case "CREATED", "SCHEDULED" -> "QUEUED";
            case "SENT" -> "DELIVERED";
            default -> "UNKNOWN";
        };
    }

    private Instant resolveAttemptTime(AssignmentReminderRequest request) {
        LocalDateTime time = request.getDueDate() != null
                ? request.getDueDate()
                : LocalDateTime.now();

        return toInstant(time);
    }

    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant();
    }

    // 🔥 implement this with DB / user service
    private List<UUID> getUsersByCourse(Long courseId) {
        return new ArrayList<>();
    }
}