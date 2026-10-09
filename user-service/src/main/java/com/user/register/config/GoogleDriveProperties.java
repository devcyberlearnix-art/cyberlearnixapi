package com.user.register.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validates Google Drive configuration at startup in user-service.
 * Enforces that folder-id and credentials must be present.
 */
@Slf4j
@Getter
@Configuration
public class GoogleDriveProperties {

    @Value("${google.client-id:}")
    private String clientId;

    @Value("${google.client-secret:}")
    private String clientSecret;

    @Value("${google.refresh-token:}")
    private String refreshToken;

    @Value("${google.folder-id:}")
    private String folderId;

    @PostConstruct
    public void validateProperties() {
        StringBuilder errors = new StringBuilder();

        if (isBlank(clientId)) errors.append("  ✖ google.client-id is MISSING\n");
        if (isBlank(clientSecret)) errors.append("  ✖ google.client-secret is MISSING\n");
        if (isBlank(refreshToken)) errors.append("  ✖ google.refresh-token is MISSING\n");
        if (isBlank(folderId)) errors.append("  ✖ google.folder-id is MISSING\n");

        if (!errors.isEmpty()) {
            String message = """
                
                ╔══════════════════════════════════════════════════════════╗
                ║  ❌ GOOGLE DRIVE CONFIGURATION VALIDATION FAILED        ║
                ╠══════════════════════════════════════════════════════════╣
                %s
                ║  Application CANNOT start without valid Google Drive    ║
                ║  credentials and folder-id.                             ║
                ╚══════════════════════════════════════════════════════════╝
                """.formatted(errors.toString());
            log.error(message);
            throw new IllegalStateException(
                "Google Drive configuration validation failed. Missing required properties: " + errors
            );
        }

        log.info("  ✔ google.client-id     : {}...{}", clientId.substring(0, 8), clientId.substring(clientId.length() - 4));
        log.info("  ✔ google.folder-id     : {}", folderId);
        log.info("  ✅ Google Drive properties validated successfully.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
