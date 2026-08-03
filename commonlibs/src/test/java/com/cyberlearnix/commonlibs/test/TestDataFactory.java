package com.cyberlearnix.commonlibs.test;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Factory for creating test data - use this in service tests
 */
public class TestDataFactory {
    public static SharedUserData createTestUserData() {
        return new SharedUserData(
                UUID.randomUUID(),
                "+919999999999",
                "Test User",
                "test@CyberLearnix.com",
                "CONSUMER",
                true,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );
    }

    public static SharedUserData createTestUserData(String phoneNumber) {
        SharedUserData user = createTestUserData();
        return new SharedUserData(
                user.userId(),
                phoneNumber,
                user.fullName(),
                user.email(),
                user.role(),
                user.active(),
                user.createdAt(),
                user.updatedAt()
        );
    }

    public static String generateTestPhoneNumber() {
        long number = 9000000000L + (long)(Math.random() * 1000000000L);
        return "+91" + number;
    }

    public record SharedUserData(
            UUID userId,
            String phoneNumber,
            String fullName,
            String email,
            String role,
            boolean active,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {}
}
