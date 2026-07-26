package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AdminProfileResponse {

    private boolean success;
    private String message;
    private String timestamp;
    private DataInfo data;

    @Data
    @Builder
    public static class DataInfo {
        private AdminInfo admin;
        private String ipAddress;
        private String device;
    }

    @Data
    @Builder
    public static class AdminInfo {
        private UUID id;
        private String email;
        private String role;
    }
}