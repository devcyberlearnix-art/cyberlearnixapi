package com.lms.review.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserApiResponse {
    private boolean success;
    private String message;
    private UserData data;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserData {
        private String firstName;
        private String lastName;
        private String email;
        private UUID userId;
    }

    public String resolveDisplayName() {
        if (data == null) {
            return null;
        }
        String firstName = data.getFirstName() != null ? data.getFirstName().trim() : "";
        String lastName = data.getLastName() != null ? data.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? null : fullName;
    }
}
