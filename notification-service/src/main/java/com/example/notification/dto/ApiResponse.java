package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ApiResponse<T> {

    private String requestId;
    private LocalDateTime timestamp;
    private String status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .message(message)
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .message(message)
                .requestId(UUID.randomUUID().toString())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .message(message)
                .requestId(UUID.randomUUID().toString())
                .build();
    }
}