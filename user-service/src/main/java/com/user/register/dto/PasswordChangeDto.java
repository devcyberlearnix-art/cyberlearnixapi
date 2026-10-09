package com.user.register.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for Step 1 of the password change flow.
 */
@Data
public class PasswordChangeDto {

    @NotBlank(message = "Old password is required.")
    private String oldPassword;

    @NotBlank(message = "New password is required.")
    private String newPassword;

    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;
}
