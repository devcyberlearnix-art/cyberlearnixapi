package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.UserProfileResponse;
import com.user.register.security.UnifiedJwtService;
import com.user.register.service.UserService;
import com.user.register.service.TokenBlacklistService;
import com.cyberlearnix.security.ServiceAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

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
    private ServiceAuthUtil serviceAuthUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(unifiedJwtService.validateToken(any())).thenReturn(false);
        userController = new UserController(userService, unifiedJwtService, serviceAuthUtil, tokenBlacklistService);
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

    @Test
    void uploadPhotoPublic_success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5}
        );
        String expectedUrl = "https://drive.google.com/uc?id=test-file-id";
        when(userService.uploadPhotoPublic(any())).thenReturn(expectedUrl);

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.uploadPhotoPublic(file);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Photo uploaded successfully", response.getBody().getMessage());
        assertEquals(expectedUrl, response.getBody().getData().get("url"));
        verify(userService, times(1)).uploadPhotoPublic(any());
    }

    @Test
    void uploadPhotoPublic_missingFile_returns400() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );
        when(userService.uploadPhotoPublic(any())).thenThrow(new RuntimeException("No file uploaded"));

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.uploadPhotoPublic(emptyFile);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("No file uploaded", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void uploadPhotoPublic_invalidMimeType_returns400() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[]{1, 2, 3}
        );
        when(userService.uploadPhotoPublic(any())).thenThrow(new RuntimeException("Only JPG, PNG, or WEBP files are allowed"));

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.uploadPhotoPublic(invalidFile);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Only JPG, PNG, or WEBP files are allowed", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void uploadPhotoPublic_oauthFailure_returns400() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );
        when(userService.uploadPhotoPublic(any()))
                .thenThrow(new RuntimeException("Google Drive OAuth authentication failed: refresh token is invalid or revoked."));

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.uploadPhotoPublic(file);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Google Drive OAuth authentication failed"));
        assertNull(response.getBody().getData());
    }

    @Test
    void uploadPhotoPublic_googleDriveApiFailure_returns400() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );
        when(userService.uploadPhotoPublic(any()))
                .thenThrow(new RuntimeException("Failed to upload to Google Drive"));

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.uploadPhotoPublic(file);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to upload to Google Drive", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
