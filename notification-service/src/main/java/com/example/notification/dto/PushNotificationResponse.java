package com.example.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushNotificationResponse {

    private int successCount;
    private int failureCount;

    /**
     * Message ID returned by Firebase (only for single-device sends).
     */
    private String messageId;

    /**
     * Topic name (only for topic sends).
     */
    private String topic;

    private String status;
    private LocalDateTime timestamp;
}
