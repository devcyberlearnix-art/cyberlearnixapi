package com.example.notification.service;

import com.example.notification.dto.UserNotificationResponse;
import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserNotificationService {

    private final NotificationRepository notificationRepository;

    public UserNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    public List<UserNotificationResponse> getMyNotifications() {

        var auth = SecurityContextHolder.getContext().getAuthentication();

// ✅ handle missing or invalid auth
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            // No authentication information – return empty list
            return List.of();
        }

        String userId = (String) auth.getPrincipal();
        UUID uuid = UUID.fromString(userId);

        List<Notification> notifications =
                notificationRepository.findByUserIdsContaining(uuid);

        return notifications.stream()
                .filter(n -> !n.isDeleted())
                .map(notification -> UserNotificationResponse.builder()
                        .notificationId(notification.getId())
                        .title(notification.getTitle())
                        .message(notification.getMessage())
                        .priority(notification.getPriority().name())
                        .status(notification.getStatus().name())
                        .createdAt(notification.getCreatedAt())
                        .read(notification.getStatus() == NotificationStatus.READ)
                        .channels(
                                notification.getChannels().stream()
                                        .map(String::toUpperCase)
                                        .toList()
                        )
                        .build()
                )
                .toList();

    }
    public List<UserNotificationResponse> getUnreadNotifications() {

        var auth = SecurityContextHolder.getContext().getAuthentication();

// ✅ Validate JWT user
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is missing or expired");
        }

        String userId = (String) auth.getPrincipal();
        UUID uuid = UUID.fromString(userId);

// ✅ Fetch notifications
        List<Notification> notifications =
                notificationRepository.findByUserIdsContaining(uuid);

        return notifications.stream()
                .filter(n -> !n.isDeleted())
                .filter(n -> n.getStatus() != NotificationStatus.READ) // 🔥 only unread
                .map(notification -> UserNotificationResponse.builder()
                        .notificationId(notification.getId())
                        .title(notification.getTitle())
                        .message(notification.getMessage())
                        .priority(notification.getPriority().name())
                        .status(notification.getStatus().name())
                        .createdAt(notification.getCreatedAt())
                        .read(false) // always unread here
                        .channels(
                                notification.getChannels().stream()
                                        .map(String::toUpperCase)
                                        .toList()
                        )
                        .build()
                )
                .toList();

    }
    public UserNotificationResponse markAsRead(String notificationId) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

// ✅ validate JWT
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is missing or expired");
        }

        String userId = (String) auth.getPrincipal();
        UUID userUuid = UUID.fromString(userId);
        UUID notifUuid = UUID.fromString(notificationId);

// ✅ fetch notification
        Notification notification = notificationRepository.findById(notifUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

// ✅ security check (user must belong to notification)
        if (!notification.getUserIds().contains(userUuid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

// ✅ already read check
        if (notification.getStatus() == NotificationStatus.READ) {
            // return same response (idempotent)
            return mapToResponse(notification, true);
        }

// ✅ mark as READ
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);

        return mapToResponse(notification, true);

    }
    private UserNotificationResponse mapToResponse(Notification notification, boolean isRead) {

        return UserNotificationResponse.builder()
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .priority(notification.getPriority().name())

                // 🔥 dynamic status
                .status(isRead ? "READ" : "UNREAD")

                .createdAt(notification.getCreatedAt())
                .read(isRead)
                .channels(
                        notification.getChannels().stream()
                                .map(String::toUpperCase)
                                .toList()
                )
                .build();

    }
    public List<UserNotificationResponse> markAllAsRead() {

        var auth = SecurityContextHolder.getContext().getAuthentication();

// ✅ JWT validation
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is missing or expired");
        }

        String userId = (String) auth.getPrincipal();
        UUID userUuid = UUID.fromString(userId);

        List<Notification> notifications =
                notificationRepository.findByUserIdsContaining(userUuid);

// ✅ filter only unread
        List<Notification> unreadNotifications = notifications.stream()
                .filter(n -> !n.isDeleted())
                .filter(n -> n.getStatus() != NotificationStatus.READ)
                .toList();

// ✅ mark all as READ
        unreadNotifications.forEach(n -> n.setStatus(NotificationStatus.READ));

        notificationRepository.saveAll(unreadNotifications);

// ✅ return updated response
        return unreadNotifications.stream()
                .map(n -> UserNotificationResponse.builder()
                        .notificationId(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .priority(n.getPriority().name())
                        .status("READ")
                        .createdAt(n.getCreatedAt())
                        .read(true)
                        .channels(
                                n.getChannels().stream()
                                        .map(String::toUpperCase)
                                        .toList()
                        )
                        .build()
                )
                .toList();

    }
    public int getUnreadCount() {

        var auth = SecurityContextHolder.getContext().getAuthentication();

// ✅ JWT validation
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is missing or expired");
        }

        String userId = (String) auth.getPrincipal();
        UUID userUuid = UUID.fromString(userId);

        List<Notification> notifications =
                notificationRepository.findByUserIdsContaining(userUuid);

        return (int) notifications.stream()
                .filter(n -> !n.isDeleted())
                .filter(n -> n.getStatus() != NotificationStatus.READ) // ✅ unread only
                .count();

    }
}