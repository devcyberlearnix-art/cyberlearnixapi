package com.lms.courseservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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
            throw new IllegalStateException(
                "Google Drive configuration validation failed in course-service: " + errors
            );
        }
        log.info("✅ Google Drive properties validated for course-service.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
