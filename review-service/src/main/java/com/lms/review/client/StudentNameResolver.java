package com.lms.review.client;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves student display names for public review listings.
 * Uses UserClient when enabled; otherwise returns a placeholder.
 */
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
                UserResponse user = userClient.getUserById(userId);
                String displayName = user != null ? user.resolveDisplayName() : null;
                if (displayName != null && !displayName.isBlank()) {
                    return displayName;
                }
            } catch (Exception ex) {
                // Fall through to placeholder
            }
        }
        // TODO: Integrate with user-service when available in all environments
        return "Student " + userId;
    }
}
