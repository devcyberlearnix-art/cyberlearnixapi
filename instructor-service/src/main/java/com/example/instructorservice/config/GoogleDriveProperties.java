package com.example.instructorservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
public class GoogleDriveProperties {

    @Value("${google.client-id:dummy-client-id}")
    private String clientId;

    @Value("${google.client-secret:dummy-client-secret}")
    private String clientSecret;

    @Value("${google.refresh-token:dummy-refresh-token}")
    private String refreshToken;

    @Value("${google.folder-id:1nLiT67PMJLS_fDw4M_jJ_qOdA0qenQYJ}")
    private String folderId;

    public boolean isConfigured() {
        return !isBlank(clientId) && !isBlank(clientSecret) && !isBlank(refreshToken) && !isBlank(folderId)
                && !"dummy-client-id".equalsIgnoreCase(clientId)
                && !"dummy-client-secret".equalsIgnoreCase(clientSecret)
                && !"dummy-refresh-token".equalsIgnoreCase(refreshToken);
    }

    @PostConstruct
    public void validateProperties() {
        if (!isConfigured()) {
            log.warn("⚠️ Google Drive properties are not configured with real credentials (using dummy/empty values) in instructor-service. Live Google Drive uploads will be simulated.");
            return;
        }
        log.info("✅ Google Drive properties validated for instructor-service.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
