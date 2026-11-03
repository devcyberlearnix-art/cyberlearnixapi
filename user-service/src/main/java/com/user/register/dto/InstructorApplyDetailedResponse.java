package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstructorApplyDetailedResponse {

    private ApplicationInfo application;
    private UserInfo user;
    private DocumentsInfo documents;
    private List<String> nextSteps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationInfo {
        private UUID applicationId;
        private String status;
        private String reviewMessage;
        private LocalDateTime submittedAt;
        private LocalDateTime reviewedAt;
        private String reviewedBy;
        private String rejectionReason;
        private String adminNotes;
        private String requestedInfo;

        private String subject;
        private String specialization;
        private String experience;
        private String bio;
        private String phone;
        private String linkedinUrl;
        private String websiteUrl;

        private String bankAccountNumber;
        private String bankIfsc;
        private String bankName;
        private String panNumber;
        private String additionalNotes;

        private Map<String, String> documentUrls;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID userId;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String mobile;
        private String profilePhoto;
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
