package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminInstructorApplicationsResponse {
    private boolean success;
    private String message;
    private List<InstructorApplicationDetail> data;
    private String timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorApplicationDetail {
        private ApplicationInfo application;
        private UserInfo user;
        private DocumentsInfo documents;
        private List<String> nextSteps;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationInfo {
        private UUID applicationId;
        private String status;
        private String reviewMessage;
        private String submittedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID userId;
        private String email;
        private String currentRole;
        private String appliedRole;
        private String accountStatus;
        private Boolean isInstructorApproved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentsInfo {
        private Map<String, Boolean> required;
        private Map<String, Boolean> optional;
    }
}
