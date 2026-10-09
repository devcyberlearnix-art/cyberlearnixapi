package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response payload carrying status of the password change session.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordChangeResponse {

    private String sessionId;
    private String status;
    private LocalDateTime expiresAt;
    private String message;
    private Boolean requiresReLogin;
}
