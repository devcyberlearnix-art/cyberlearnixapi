package com.example.admin.dto;

import com.example.admin.validation.ValidLanguage;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminProfileRequest {

    @Size(min = 1, max = 50, message = "firstName must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "firstName contains invalid characters")
    private String firstName;

    @Size(min = 1, max = 50, message = "lastName must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "lastName contains invalid characters")
    private String lastName;

    @Size(max = 500, message = "profilePhoto URL must not exceed 500 characters")
    @Pattern(
        regexp = "^(https?://).+$",
        message = "profilePhoto must be a valid HTTP/HTTPS URL"
    )
    private String profilePhoto;

    @ValidLanguage
    private String preferredLanguage;

    @Size(max = 50, message = "city must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]*$", message = "city contains invalid characters")
    private String city;

    @Size(max = 50, message = "state must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]*$", message = "state contains invalid characters")
    private String state;

    @Size(max = 50, message = "country must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]*$", message = "country contains invalid characters")
    private String country;
}