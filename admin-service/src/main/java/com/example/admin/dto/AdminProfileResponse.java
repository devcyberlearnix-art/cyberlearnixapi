package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminProfileResponse {

    private boolean success;
    private String message;
    private String timestamp;
    private DataInfo data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DataInfo {
        private AdminInfo admin;
        private String ipAddress;
        private String device;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdminInfo {
        private UUID id;
        private String email;
        private String role;
        private String adminType;
        private String firstName;
        private String lastName;
        private String profilePhoto;
        private String preferredLanguage;
        private String city;
        private String state;
        private String country;
        private String updatedAt;
    }
}