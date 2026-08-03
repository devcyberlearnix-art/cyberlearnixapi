package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LogoutResponse {

    private boolean success;
    private String message;
    private String timestamp;
    private LogoutData data;

    @Data
    @Builder
    public static class LogoutData {
        private Admin admin;
        private LogoutSession logoutSession;
    }

    @Data
    @Builder
    public static class Admin {
        private UUID id;
        private String email;
        private String role;
    }

    @Data
    @Builder
    public static class LogoutSession {
        private String ipAddress;
        private String device;
        private String logoutTime;
        private String sessionStatus;
    }
}