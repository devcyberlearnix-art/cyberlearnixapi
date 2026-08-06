package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstructorApplyDetailedResponse {

    private ApplicationInfo application;
    private UserInfo user;
    private DocumentsInfo documents;
    private List<String> nextSteps;

    @Data
    @Builder
    public static class ApplicationInfo {
        private UUID applicationId;
        private String status;
        private String reviewMessage;
        private LocalDateTime submittedAt;
    }

    @Data
    @Builder
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
    public static class DocumentsInfo {
        private Map<String, Boolean> required;
        private Map<String, Boolean> optional;
    }
}
