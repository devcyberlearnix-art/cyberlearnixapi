package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PasswordResetResponse {
    private boolean success;
    private String message;
    private AdminInfo data;
    private String timestamp;

    @Data
    @Builder
    public static class AdminInfo {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
    }
}