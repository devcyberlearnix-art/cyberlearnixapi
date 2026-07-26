package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class DlqReprocessResponse {

    private Instant timestamp;
    private int status;
    private String message;
    private DataPayload data;

    @Data
    @Builder
    public static class DataPayload {
        private int totalMessages;
        private int successCount;
        private int failureCount;
        private List<FailedMessage> failedMessages;
    }

    @Data
    @Builder
    public static class FailedMessage {
        private String notificationId;
        private String error;
    }
}