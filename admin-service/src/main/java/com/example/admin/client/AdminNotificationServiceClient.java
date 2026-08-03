package com.example.admin.client;

import com.example.admin.security.JwtService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AdminNotificationServiceClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${notification-service.url:http://localhost:8093}")
    private String notificationServiceUrl;

    public AdminNotificationServiceClient(RestTemplate restTemplate, JwtService jwtService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
    }

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    public List<NotificationDTO> getAllNotifications() {
        try {
            String url = notificationServiceUrl + "/api/v1/notifications";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseNotificationList(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get notifications from Notification Service: " + e.getMessage());
            return List.of();
        }
    }

    public NotificationDTO getNotificationById(String id) {
        try {
            String url = notificationServiceUrl + "/api/v1/notifications/" + id;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToNotificationDto(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get notification from Notification Service: " + e.getMessage());
            return null;
        }
    }

    public List<NotificationDTO> getNotificationsByUserId(String userId) {
        try {
            String url = notificationServiceUrl + "/api/v1/notifications/user/" + userId;
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseNotificationList(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get notifications for user: " + e.getMessage());
            return List.of();
        }
    }

    private List<NotificationDTO> parseNotificationList(Object[] body) {
        if (body == null) {
            return List.of();
        }
        List<NotificationDTO> result = new ArrayList<>();
        for (Object item : body) {
            result.add(mapToNotificationDto(item));
        }
        return result;
    }

    private NotificationDTO mapToNotificationDto(Object body) {
        if (body == null) {
            return null;
        }
        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                return mapFromMap(dataMap);
            }
            return mapFromMap(map);
        }
        return null;
    }

    private NotificationDTO mapFromMap(Map<?, ?> map) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(getString(map.get("id")));
        dto.setTitle(getString(map.get("title")));
        dto.setMessage(getString(map.get("message")));
        dto.setType(getString(map.get("type")));
        dto.setUserId(getString(map.get("userId")));
        dto.setCourseId(getLong(map.get("courseId")));
        dto.setStatus(getString(map.get("status")));
        dto.setCreatedAt(getString(map.get("createdAt")));
        return dto;
    }

    private String getString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long getLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NotificationDTO {
        private String id;
        private String title;
        private String message;
        private String type;
        private String userId;
        private Long courseId;
        private String status;
        private String createdAt;
    }
}
