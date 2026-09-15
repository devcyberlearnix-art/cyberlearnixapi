package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.UserProfileResponse;
import com.user.register.security.UnifiedJwtService;
import com.user.register.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private UnifiedJwtService unifiedJwtService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(unifiedJwtService.validateToken(any())).thenReturn(false);
        userController = new UserController(userService, unifiedJwtService);
    }

    @Test
    void getProfile_success() {
        // Arrange
        UserProfileResponse mockProfile = UserProfileResponse.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
        when(userService.getLoggedInUserProfile(request)).thenReturn(mockProfile);

        // Act
        ResponseEntity<ApiResponse<UserProfileResponse>> response = userController.getProfile(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User profile fetched successfully", response.getBody().getMessage());
        assertEquals(mockProfile, response.getBody().getData());
        verify(userService, times(1)).getLoggedInUserProfile(request);
    }

    @Test
    void getProfile_runtimeException_returns400() {
        // Arrange
        when(userService.getLoggedInUserProfile(request)).thenThrow(new RuntimeException("Invalid token"));

        // Act
        ResponseEntity<ApiResponse<UserProfileResponse>> response = userController.getProfile(request);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Missing or invalid Authorization header", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
