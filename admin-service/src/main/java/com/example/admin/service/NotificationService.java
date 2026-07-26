package com.example.admin.service;

import com.example.admin.client.AdminNotificationServiceClient;
import com.example.admin.client.AdminNotificationServiceClient.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AdminNotificationServiceClient notificationServiceClient;

    public List<NotificationDTO> getAllNotifications() {
        return notificationServiceClient.getAllNotifications();
    }

    public NotificationDTO getNotificationById(String id) {
        return notificationServiceClient.getNotificationById(id);
    }

    public List<NotificationDTO> getNotificationsByUserId(String userId) {
        return notificationServiceClient.getNotificationsByUserId(userId);
    }
}
