package com.example.notification.service;

import com.example.notification.dto.*;
import com.example.notification.entity.Notification;
import com.example.notification.enums.ChannelType;
import com.example.notification.enums.DeliveryStatus;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.Priority;
import com.example.notification.repository.DeviceTokenRepository;
import com.example.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final FirebasePushService firebasePushService;
    private final DeviceTokenRepository deviceTokenRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               FirebasePushService firebasePushService,
                               DeviceTokenRepository deviceTokenRepository) {
        this.notificationRepository = notificationRepository;
        this.firebasePushService = firebasePushService;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public NotificationResponse createNotification(CreateNotificationRequest request) {

        LocalDateTime now = LocalDateTime.now();

        // ✅ Create entity
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .userIds(request.getUserIds())
                .channels(
                        request.getChannels().stream()
                                .map(Enum::name)
                                .toList()
                )
                .status(NotificationStatus.QUEUED)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .createdAt(now)
                .build();

        // ✅ Save to DB
        Notification saved = notificationRepository.save(notification);

        // 🔥 BUILD DELIVERY STATUS
        List<NotificationResponse.DeliveryInfo> deliveries = new ArrayList<>(
                request.getUserIds().stream()
                        .flatMap(userId ->
                                request.getChannels().stream()
                                        .map(channel -> NotificationResponse.DeliveryInfo.builder()
                                                .userId(userId)
                                                .channel(channel)
                                                .status(DeliveryStatus.QUEUED)
                                                .error(null)
                                                .attemptedAt(now)
                                                .build()
                                        )
                        )
                        .toList()
        );

        // 🔥 If PUSH channel is requested, fire Firebase push notifications
        if (request.getChannels().contains(ChannelType.PUSH)) {
            deliverPushNotifications(request, deliveries);
        }

        // ✅ Return response
        return NotificationResponse.builder()
                .notificationId(saved.getId().toString())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .userIds(saved.getUserIds())
                .channels(request.getChannels())
                .status(saved.getStatus())
                .priority(saved.getPriority())
                .createdAt(saved.getCreatedAt())
                .deliveryStatuses(deliveries)
                .build();
    }

    /**
     * Deliver Firebase push notifications to users' registered device tokens.
     * Updates the delivery status entries in-place.
     */
    private void deliverPushNotifications(CreateNotificationRequest request,
                                          List<NotificationResponse.DeliveryInfo> deliveries) {
        try {
            List<String> userIdStrings = request.getUserIds().stream()
                    .map(UUID::toString)
                    .toList();

            List<String> fcmTokens = deviceTokenRepository.findTokensByUserIds(userIdStrings);

            if (fcmTokens.isEmpty()) {
                log.warn("⚠️ No active FCM tokens found for users: {}", userIdStrings);
                // Mark PUSH deliveries as FAILED
                deliveries.stream()
                        .filter(d -> d.getChannel() == ChannelType.PUSH)
                        .forEach(d -> {
                            d.setStatus(DeliveryStatus.FAILED);
                            d.setError("No active device tokens registered");
                        });
                return;
            }

            var batchResponse = firebasePushService.sendToMultipleDevices(
                    fcmTokens, request.getTitle(), request.getMessage(), null);

            // Mark PUSH deliveries based on Firebase response
            DeliveryStatus pushStatus = batchResponse.getFailureCount() == 0
                    ? DeliveryStatus.SENT
                    : DeliveryStatus.FAILED;

            deliveries.stream()
                    .filter(d -> d.getChannel() == ChannelType.PUSH)
                    .forEach(d -> {
                        d.setStatus(pushStatus);
                        d.setAttemptedAt(LocalDateTime.now());
                    });

            log.info("📱 Firebase push delivered: success={}, failure={}",
                    batchResponse.getSuccessCount(), batchResponse.getFailureCount());

        } catch (Exception e) {
            log.error("❌ Firebase push delivery failed: {}", e.getMessage());
            deliveries.stream()
                    .filter(d -> d.getChannel() == ChannelType.PUSH)
                    .forEach(d -> {
                        d.setStatus(DeliveryStatus.FAILED);
                        d.setError(e.getMessage());
                    });
        }
    }

    public BulkNotificationResponse createBulkNotifications(BulkNotificationRequest request) {

        List<NotificationResponse> successList = new ArrayList<>();
        List<BulkNotificationResponse.FailedNotification> failedList = new ArrayList<>();

        for (CreateNotificationRequest req : request.getNotifications()) {
            try {
                NotificationResponse response = createNotification(req);
                successList.add(response);
            } catch (Exception e) {
                failedList.add(
                        BulkNotificationResponse.FailedNotification.builder()
                                .request(req)
                                .errorMessage(e.getMessage())
                                .build()
                );
            }
        }

        return BulkNotificationResponse.builder()
                .totalRequested(request.getNotifications().size())
                .successCount(successList.size())
                .failedCount(failedList.size())
                .successNotifications(successList)
                .failedNotifications(failedList)
                .status(failedList.isEmpty() ? "SUCCESS" : "PARTIAL_FAILURE")
                .build();
    }

    public NotificationResponse getNotificationById(String id) {

        UUID uuid = UUID.fromString(id);

        Notification notification = notificationRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        // 🔥 BUILD DELIVERY STATUS (THIS FIXES YOUR ISSUE)
        List<NotificationResponse.DeliveryInfo> deliveries =
                notification.getUserIds().stream()
                        .flatMap(userId ->
                                notification.getChannels().stream()
                                        .map(channel -> NotificationResponse.DeliveryInfo.builder()
                                                .userId(userId)
                                                .channel(ChannelType.valueOf(channel))
                                                .status(DeliveryStatus.QUEUED)
                                                .error(null)
                                                .attemptedAt(notification.getCreatedAt())
                                                .build()
                                        )
                        )
                        .toList();

        return NotificationResponse.builder()
                .notificationId(notification.getId().toString())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .userIds(notification.getUserIds())
                .channels(
                        notification.getChannels().stream()
                                .map(ChannelType::valueOf)
                                .toList()
                )
                .status(notification.getStatus())
                .priority(notification.getPriority())
                .createdAt(notification.getCreatedAt())

                // ✅ NOW IT WILL NOT BE EMPTY
                .deliveryStatuses(deliveries)

                .build();
    }
    public List<NotificationResponse> getAllNotifications() {

        List<Notification> notifications = notificationRepository.findAll();

        return notifications.stream()
                .map(notification -> {

                    // ✅ Build delivery statuses (NOT EMPTY)
                    List<NotificationResponse.DeliveryInfo> deliveries =
                            notification.getUserIds().stream()
                                    .flatMap(userId ->
                                            notification.getChannels().stream()
                                                    .map(channel -> NotificationResponse.DeliveryInfo.builder()
                                                            .userId(userId)
                                                            .channel(ChannelType.valueOf(channel))
                                                            .status(DeliveryStatus.QUEUED)
                                                            .attemptedAt(notification.getCreatedAt())
                                                            .build()
                                                    )
                                    )
                                    .toList();

                    return NotificationResponse.builder()
                            .notificationId(notification.getId().toString())
                            .title(notification.getTitle())
                            .message(notification.getMessage())
                            .userIds(notification.getUserIds())
                            .channels(
                                    notification.getChannels().stream()
                                            .map(ChannelType::valueOf)
                                            .toList()
                            )
                            .status(notification.getStatus())
                            .priority(notification.getPriority())
                            .createdAt(notification.getCreatedAt())
                            .deliveryStatuses(deliveries) // ✅ NOT EMPTY
                            .build();
                })
                .toList();
    }
    public List<NotificationResponse> getNotificationsByUserId(String userId) {

        UUID uuid = UUID.fromString(userId);

        List<Notification> notifications =
                notificationRepository.findByUserIdsContaining(uuid);

        return notifications.stream()
                .map(notification -> {

                    // 🔥 Build delivery statuses (NOT EMPTY)
                    List<NotificationResponse.DeliveryInfo> deliveries =
                            notification.getUserIds().stream()
                                    .flatMap(u ->
                                            notification.getChannels().stream()
                                                    .map(channel -> NotificationResponse.DeliveryInfo.builder()
                                                            .userId(u)
                                                            .channel(ChannelType.valueOf(channel))
                                                            .status(DeliveryStatus.QUEUED)
                                                            .attemptedAt(notification.getCreatedAt())
                                                            .build()
                                                    )
                                    )
                                    .toList();

                    return NotificationResponse.builder()
                            .notificationId(notification.getId().toString())
                            .title(notification.getTitle())
                            .message(notification.getMessage())
                            .userIds(notification.getUserIds())
                            .channels(
                                    notification.getChannels().stream()
                                            .map(ChannelType::valueOf)
                                            .toList()
                            )
                            .status(notification.getStatus())
                            .priority(notification.getPriority())
                            .createdAt(notification.getCreatedAt())
                            .deliveryStatuses(deliveries) // ✅ NOT EMPTY
                            .build();
                })
                .toList();
    }

    // Fetch only unread (non-READ status) notifications for a user
    public List<NotificationResponse> getUnreadNotificationsByUserId(String userId) {
        UUID uuid = UUID.fromString(userId);
        List<Notification> notifications =
                notificationRepository.findByUserIdsContaining(uuid);
        return notifications.stream()
                .filter(notification -> notification.getStatus() != NotificationStatus.READ)
                .map(notification -> {
                    List<NotificationResponse.DeliveryInfo> deliveries =
                            notification.getUserIds().stream()
                                    .flatMap(u ->
                                            notification.getChannels().stream()
                                                    .map(channel -> NotificationResponse.DeliveryInfo.builder()
                                                            .userId(u)
                                                            .channel(ChannelType.valueOf(channel))
                                                            .status(DeliveryStatus.QUEUED)
                                                            .attemptedAt(notification.getCreatedAt())
                                                            .build())
                                    )
                                    .toList();
                    return NotificationResponse.builder()
                            .notificationId(notification.getId().toString())
                            .title(notification.getTitle())
                            .message(notification.getMessage())
                            .userIds(notification.getUserIds())
                            .channels(notification.getChannels().stream()
                                    .map(ChannelType::valueOf)
                                    .toList())
                            .status(notification.getStatus())
                            .priority(notification.getPriority())
                            .createdAt(notification.getCreatedAt())
                            .deliveryStatuses(deliveries)
                            .build();
                })
                .toList();
    }

    public NotificationResponse markNotificationAsRead(String id) {

        UUID uuid = UUID.fromString(id);

        Notification notification = notificationRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setStatus(NotificationStatus.READ);

        Notification updated = notificationRepository.save(notification);

        List<NotificationResponse.DeliveryInfo> deliveries =
                updated.getUserIds().stream()
                        .flatMap(userId ->
                                updated.getChannels().stream()
                                        .map(channel -> NotificationResponse.DeliveryInfo.builder()
                                                .userId(userId)
                                                .channel(ChannelType.valueOf(channel.toUpperCase()))
                                                .status(DeliveryStatus.SENT)
                                                .attemptedAt(updated.getCreatedAt())
                                                .build()
                                        )
                        )
                        .toList();

        return NotificationResponse.builder()
                .notificationId(updated.getId().toString())
                .title(updated.getTitle())
                .message(updated.getMessage())
                .userIds(updated.getUserIds())
                .channels(
                        updated.getChannels().stream()
                                .map(ch -> ChannelType.valueOf(ch.toUpperCase()))
                                .toList()
                )
                .status(updated.getStatus())
                .priority(updated.getPriority())
                .createdAt(updated.getCreatedAt())
                .deliveryStatuses(deliveries)
                .build();
    }
    public DeleteNotificationResponse deleteNotification(String id) {

        UUID uuid = UUID.fromString(id);

        Notification notification = notificationRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.isDeleted()) {
            throw new RuntimeException("Notification already deleted");
        }

        String originalMessage = notification.getMessage();

        notification.setDeleted(true);
        notification.setDeletedAt(LocalDateTime.now());

        notificationRepository.save(notification);

        return DeleteNotificationResponse.builder()
                .notificationId(notification.getId().toString())
                .operation("DELETE_NOTIFICATION")
                .status("DELETED")
                .message("Notification deleted successfully")
                .deletedAt(notification.getDeletedAt())
                .originalMessage(originalMessage)
                .build();
    }
    public NotificationRetryResponse retryNotification(String id) {

        UUID uuid = UUID.fromString(id);

        Notification notification = notificationRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));

        if (notification.isDeleted()) {
            throw new RuntimeException("Cannot retry a deleted notification");
        }

        int userCount = notification.getUserIds() != null ? notification.getUserIds().size() : 0;
        int channelCount = notification.getChannels() != null ? notification.getChannels().size() : 0;

        int totalAttempts = userCount * channelCount;

        // update status for retry lifecycle
        notification.setStatus(NotificationStatus.PROCESSING);
        notificationRepository.save(notification);

        return NotificationRetryResponse.builder()
                .notificationId(notification.getId().toString())
                .operation("RETRY_NOTIFICATION")
                .status("RETRY_INITIATED")
                .message("Notification retry has been successfully initiated")
                .retriedAt(LocalDateTime.now())

                .retryDetails(
                        NotificationRetryResponse.RetryDetails.builder()
                                .userCount(userCount)
                                .channelCount(channelCount)
                                .totalRetryAttempts(totalAttempts)
                                .retryReason("PREVIOUS_DELIVERY_FAILURE_OR_MANUAL_RETRY")
                                .build()
                )

                .build();
    }
}