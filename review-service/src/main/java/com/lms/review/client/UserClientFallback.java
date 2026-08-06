package com.lms.review.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "user.service.enabled", havingValue = "true")
public class UserClientFallback implements UserClient {

    @Override
    public UserResponse getUserById(UUID userId) {
        log.warn("User service unavailable for userId={}. Using placeholder name.", userId);
        return UserResponse.builder()
                .success(false)
                .build();
    }
}
