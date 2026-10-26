package com.lms.review.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentNameResolverTest {

    @Mock
    private UserClient userClient;

    private StudentNameResolver resolver;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    @Test
    void resolve_WhenUserServiceEnabledAndUserFound_ReturnsDisplayName() {
        // Arrange
        resolver = new StudentNameResolver(userClient, true);
        UserApiResponse response = UserApiResponse.builder()
                .success(true)
                .data(UserApiResponse.UserData.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .userId(testUserId)
                        .build())
                .build();
        when(userClient.getUserById(testUserId)).thenReturn(response);

        // Act
        String result = resolver.resolve(testUserId);

        // Assert
        assertEquals("John Doe", result);
        verify(userClient, times(1)).getUserById(testUserId);
    }

    @Test
    void resolve_WhenUserServiceEnabledAndUserNotFound_ReturnsPlaceholder() {
        // Arrange
        resolver = new StudentNameResolver(userClient, true);
        UserApiResponse response = UserApiResponse.builder()
                .success(false)
                .build();
        when(userClient.getUserById(testUserId)).thenReturn(response);

        // Act
        String result = resolver.resolve(testUserId);

        // Assert
        assertEquals("Student " + testUserId, result);
        verify(userClient, times(1)).getUserById(testUserId);
    }

    @Test
    void resolve_WhenUserServiceEnabledAndUserHasEmptyName_ReturnsPlaceholder() {
        // Arrange
        resolver = new StudentNameResolver(userClient, true);
        UserApiResponse response = UserApiResponse.builder()
                .success(true)
                .data(UserApiResponse.UserData.builder()
                        .firstName("")
                        .lastName("")
                        .userId(testUserId)
                        .build())
                .build();
        when(userClient.getUserById(testUserId)).thenReturn(response);

        // Act
        String result = resolver.resolve(testUserId);

        // Assert
        assertEquals("Student " + testUserId, result);
        verify(userClient, times(1)).getUserById(testUserId);
    }

    @Test
    void resolve_WhenUserServiceEnabledAndServiceThrowsException_ReturnsPlaceholder() {
        // Arrange
        resolver = new StudentNameResolver(userClient, true);
        when(userClient.getUserById(testUserId)).thenThrow(new RuntimeException("Service unavailable"));

        // Act
        String result = resolver.resolve(testUserId);

        // Assert
        assertEquals("Student " + testUserId, result);
        verify(userClient, times(1)).getUserById(testUserId);
    }

    @Test
    void resolve_WhenUserServiceDisabled_ReturnsPlaceholder() {
        // Arrange
        resolver = new StudentNameResolver(null, false);

        // Act
        String result = resolver.resolve(testUserId);

        // Assert
        assertEquals("Student " + testUserId, result);
        verify(userClient, never()).getUserById(any());
    }

    @Test
    void resolve_WhenUserServiceEnabledButClientIsNull_ReturnsPlaceholder() {
        // Arrange
        resolver = new StudentNameResolver(null, true);

        // Act
        String result = resolver.resolve(testUserId);

        // Assert
        assertEquals("Student " + testUserId, result);
        verify(userClient, never()).getUserById(any());
    }

    @Test
    void resolve_WhenUserHasOnlyFirstName_ReturnsFirstName() {
        // Arrange
        resolver = new StudentNameResolver(userClient, true);
        UserApiResponse response = UserApiResponse.builder()
                .success(true)
                .data(UserApiResponse.UserData.builder()
                        .firstName("John")
                        .lastName(null)
                        .userId(testUserId)
                        .build())
                .build();
        when(userClient.getUserById(testUserId)).thenReturn(response);

        // Act
        String result = resolver.resolve(testUserId);

        // Assert
        assertEquals("John", result);
    }

    @Test
    void resolve_WhenUserHasOnlyLastName_ReturnsLastName() {
        // Arrange
        resolver = new StudentNameResolver(userClient, true);
        UserApiResponse response = UserApiResponse.builder()
                .success(true)
                .data(UserApiResponse.UserData.builder()
                        .firstName(null)
                        .lastName("Doe")
                        .userId(testUserId)
                        .build())
                .build();
        when(userClient.getUserById(testUserId)).thenReturn(response);

        // Act
        String result = resolver.resolve(testUserId);

        // Assert
        assertEquals("Doe", result);
    }
}
