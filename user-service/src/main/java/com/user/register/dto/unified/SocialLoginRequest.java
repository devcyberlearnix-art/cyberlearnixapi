package com.user.register.dto.unified;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    /**
     * Provider name: "google", "github", "linkedin"
     */
    @NotBlank(message = "Provider is required (google, github, linkedin)")
    private String provider;

    /**
     * User's email from OAuth provider
     */
    private String email;

    /**
     * First name from OAuth provider
     */
    private String firstName;

    /**
     * Last name from OAuth provider
     */
    private String lastName;

    /**
     * Profile photo or avatar URL
     */
    private String profilePhoto;

    /**
     * Unique user identifier provided by OAuth provider (e.g. sub or id)
     */
    private String providerId;

    /**
     * Optional OAuth2 token, id_token, or access_token from client
     */
    private String token;
}
