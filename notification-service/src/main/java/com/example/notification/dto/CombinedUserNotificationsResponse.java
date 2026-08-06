package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Response object that contains both all notifications for a user and the subset of unread notifications.
 */
@Data
@Builder
public class CombinedUserNotificationsResponse {
    /** All notifications belonging to the user. */
    private List<NotificationResponse> allNotifications;

    /** Only the unread notifications belonging to the user. */
    private List<NotificationResponse> unreadNotifications;
}
