package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    /**
     * Optional — if provided, sends to a Firebase topic (e.g., "all_users", "course_123").
     */
    private String topic;

    /**
     * Optional — list of user IDs whose registered device tokens will receive the push.
     * If both topic and userIds are null, the request is invalid.
     */
    private List<String> userIds;

    /**
     * Optional — custom key-value data payload attached to the push notification.
     */
    private Map<String, String> data;
}
