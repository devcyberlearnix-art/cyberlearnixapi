package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for Step 1 of the admin password change flow.
 */
@Data
public class AdminPasswordChangeDto {

    @NotBlank(message = "Old password is required.")
    private String oldPassword;

    @NotBlank(message = "New password is required.")
    private String newPassword;

    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;
}
