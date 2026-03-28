package com.cyberlearnix.commonlibs.test;

import com.cyberlearnix.commonlibs.entity.UserEntity;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Factory for creating test data - use this in service tests
 */
public class TestDataFactory {
    
    public static UserEntity createTestUser() {
        UserEntity user = new UserEntity();
        user.setUserId(UUID.randomUUID());
        user.setPhoneNumber("+919999999999");
        user.setFullName("Test User");
        user.setEmail("test@CyberLearnix.com");
        user.setRole("CONSUMER");
        user.setActive(true);
        user.setCreatedAt(ZonedDateTime.now());
        user.setUpdatedAt(ZonedDateTime.now());
        return user;
    }
    
    public static UserEntity createTestUser(String phoneNumber) {
        UserEntity user = createTestUser();
        user.setPhoneNumber(phoneNumber);
        return user;
    }
    
    public static String generateTestPhoneNumber() {
        long number = 9000000000L + (long)(Math.random() * 1000000000L);
        return "+91" + number;
    }
}
