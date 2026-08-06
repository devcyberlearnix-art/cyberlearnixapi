package com.example.notification.service;
import com.example.notification.dto.AnnouncementResponse;
import com.example.notification.dto.CreateAnnouncementRequest;
import com.example.notification.dto.DetailedAnnouncementResponse;
import com.example.notification.entity.Announcement;
import com.example.notification.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request) {

        // ✅ Validation logic
        if (!request.getSendToAll() && request.getCourseId() == null &&
                (request.getUserIds() == null || request.getUserIds().isEmpty())) {
            throw new RuntimeException("User IDs required when sendToAll is false and no courseId is present");
        }

        String status = (request.getScheduledAt() != null) ? "SCHEDULED" : "CREATED";

        Announcement announcement = Announcement.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle())
                .message(request.getMessage())
                .sendToAll(request.getSendToAll())
                .userIds(request.getUserIds())
                .scheduledAt(request.getScheduledAt())
                .createdAt(LocalDateTime.now())
                .status(status)
                .courseId(request.getCourseId())
                .createdBy(request.getCreatedBy())
                .build();

        Announcement saved = announcementRepository.save(announcement);

        return AnnouncementResponse.builder()
                .announcementId(saved.getId())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .sendToAll(saved.getSendToAll())
                .userIds(saved.getUserIds())
                .scheduledAt(saved.getScheduledAt())
                .createdAt(saved.getCreatedAt())
                .status(saved.getStatus())
                .courseId(saved.getCourseId())
                .createdBy(saved.getCreatedBy())
                .build();
    }
    // =========================================
    // ✅ GET DETAILED ANNOUNCEMENTS (DYNAMIC)
    // =========================================
    public List<DetailedAnnouncementResponse> getAnnouncementsDetailByCourseId(Long courseId) {

        List<Announcement> announcements =
                announcementRepository.findByCourseIdAndActiveTrueOrderByCreatedAtDesc(courseId);

        if (announcements == null || announcements.isEmpty()) {
            return Collections.emptyList();
        }

        return announcements.stream()
                .map(this::mapToDetailedDynamicResponse)
                .toList();
    }

    // =========================================
    // ✅ MAIN DYNAMIC MAPPING
    // =========================================
    private DetailedAnnouncementResponse mapToDetailedDynamicResponse(Announcement a) {

        List<UUID> users = resolveTargetUsers(a);

        String channel = resolveChannel(a);

        String deliveryStatus = deriveDeliveryStatus(a.getStatus());

        List<DetailedAnnouncementResponse.DeliveryInfo> deliveries =
                users.stream().map(userId ->
                        DetailedAnnouncementResponse.DeliveryInfo.builder()
                                .userId(userId)
                                .channel(channel)
                                .status(deliveryStatus)
                                .attemptedAt(resolveAttemptTime(a))
                                .build()
                ).toList();

        return DetailedAnnouncementResponse.builder()
                .announcementId(a.getId())
                .title(a.getTitle())
                .content(a.getMessage())
                .courseId(a.getCourseId())
                .createdBy(a.getCreatedBy() != null ? a.getCreatedBy().toString() : null)

                .createdAt(toInstant(a.getCreatedAt()))
                .updatedAt(toInstant(a.getCreatedAt()))

                .priority(resolvePriority(a))
                .status(a.getStatus())

                .channels(List.of(channel))
                .targetUsers(
                        users.stream()
                                .map(UUID::toString)
                                .toList()
                )
                .deliveryStatuses(deliveries)

                .build();
    }

    // =========================================
    // ✅ SIMPLE RESPONSE MAPPER
    // =========================================
    private AnnouncementResponse mapToResponse(Announcement a) {
        return AnnouncementResponse.builder()
                .announcementId(a.getId())
                .title(a.getTitle())
                .message(a.getMessage())
                .sendToAll(a.getSendToAll())
                .userIds(a.getUserIds())
                .scheduledAt(a.getScheduledAt())
                .createdAt(a.getCreatedAt())
                .status(a.getStatus())
                .courseId(a.getCourseId())
                .createdBy(a.getCreatedBy())
                .build();
    }

    // =========================================
    // 🔥 HELPER METHODS (DYNAMIC LOGIC)
    // =========================================

    private void validateRequest(CreateAnnouncementRequest request) {
        if (!Boolean.TRUE.equals(request.getSendToAll())
                && request.getCourseId() == null
                && (request.getUserIds() == null || request.getUserIds().isEmpty())) {
            throw new RuntimeException("User IDs required when sendToAll is false and no courseId is present");
        }
    }

    // ✅ Users
    private List<UUID> resolveTargetUsers(Announcement a) {
        if (Boolean.TRUE.equals(a.getSendToAll())) {
            return getAllUsersOfCourse(a.getCourseId());
        }
        return a.getUserIds() != null ? a.getUserIds() : Collections.emptyList();
    }

    // ✅ Channel
    private String resolveChannel(Announcement a) {
        return "IN_APP"; // later from DB/config
    }

    // ✅ Delivery Status
    private String deriveDeliveryStatus(String status) {
        if (status == null) return "QUEUED";

        return switch (status) {
            case "CREATED" -> "QUEUED";
            case "SCHEDULED" -> "QUEUED";
            case "SENT" -> "DELIVERED";
            default -> "UNKNOWN";
        };
    }

    // ✅ Priority
    private String resolvePriority(Announcement a) {

        if ("SENT".equalsIgnoreCase(a.getStatus())) return "HIGH";

        if ("SCHEDULED".equalsIgnoreCase(a.getStatus())) {
            if (a.getScheduledAt() != null &&
                    a.getScheduledAt().isAfter(LocalDateTime.now())) {
                return "MEDIUM";
            }
        }

        return "LOW";
    }

    // ✅ Attempt Time
    private Instant resolveAttemptTime(Announcement a) {
        LocalDateTime time = (a.getScheduledAt() != null)
                ? a.getScheduledAt()
                : a.getCreatedAt();

        return toInstant(time);
    }

    // ✅ Convert LocalDateTime → Instant
    private Instant toInstant(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant();
    }

    // =========================================
    // ⚠️ YOU MUST IMPLEMENT THIS
    // =========================================
    private List<UUID> getAllUsersOfCourse(Long courseId) {
        // 🔥 Call user-service or DB
        return Collections.emptyList(); // placeholder
    }


}
