package com.lms.review.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves student display names for public review listings.
 * Uses UserClient when enabled; otherwise returns a placeholder.
 */
@Slf4j
@Component
public class StudentNameResolver {

    private final UserClient userClient;
    private final boolean userServiceEnabled;

    public StudentNameResolver(
            @org.springframework.beans.factory.annotation.Autowired(required = false) UserClient userClient,
            @org.springframework.beans.factory.annotation.Value("${user.service.enabled:false}") boolean userServiceEnabled) {
        this.userClient = userClient;
        this.userServiceEnabled = userServiceEnabled;
    }

    public String resolve(UUID userId) {
        if (userServiceEnabled && userClient != null) {
            try {
                UserApiResponse response = userClient.getUserById(userId);
                if (response != null && response.isSuccess()) {
                    String displayName = response.resolveDisplayName();
                    if (displayName != null && !displayName.isBlank()) {
                        return displayName;
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed to resolve user name for userId={}: {}", userId, ex.getMessage());
                // Fall through to placeholder
            }
        }
        // Fallback to placeholder if service disabled, unavailable, or user not found
        return "Student " + userId;
    }
}
