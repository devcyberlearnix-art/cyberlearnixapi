package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Data
@Builder
public class AdminUsersResponse {

    private boolean success;
    private String message;
    private String timestamp;
    private DataInfo data;

    @Data
    @Builder
    public static class DataInfo {
        private int totalUsers;
        private List<UserInfo> users;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)

    public static class UserInfo {
        private UUID id;
        private String email;
        private String role;
        private String createdAt;
        private String status;
    }
}
