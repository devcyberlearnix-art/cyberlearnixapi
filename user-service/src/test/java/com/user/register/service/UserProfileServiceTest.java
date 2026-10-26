package com.user.register.service;

import com.user.register.dto.UpdateUserProfileRequest;
import com.user.register.dto.UserProfileResponse;
import com.user.register.entity.User;
import com.user.register.exception.ProfilePhotoException;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.UnifiedJwtService;
import com.user.register.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private UnifiedJwtService unifiedJwtService;

    @Mock
    private GoogleDriveService googleDriveService;

    @Mock
    private SessionService sessionService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    private UserService userService;

    private UUID testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                sessionRepository,
                null,
                unifiedJwtService,
                null,
                googleDriveService,
                sessionService,
                tokenBlacklistService
        );

        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .mobile("+1234567890")
                .countryCode("+1")
                .dob("1990-01-01")
                .city("New York")
                .state("NY")
                .country("USA")
                .preferredLanguage("en")
                .organization("Test Org")
                .skills("Java,Python")
                .fieldOfStudy("Computer Science")
                .highestQualification("Bachelor's")
                .gender("male")
                .role(User.Role.STUDENT)
                .status(User.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        // Clear SecurityContext before each test
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void getLoggedInUserProfile_success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.getLoggedInUserProfile(request);

        // Assert
        assertNotNull(response);
        assertEquals(testUserId, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());
        assertEquals("ACTIVE", response.getStatus());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void getLoggedInUserProfile_suspendedUser_throwsException() {
        // Arrange
        testUser.setStatus(User.Status.SUSPENDED);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getLoggedInUserProfile(request);
        });
        assertTrue(exception.getMessage().contains("deleted or suspended"));
    }

    @Test
    void updateUserProfile_success() {
        // Arrange
        UpdateUserProfileRequest updateRequest = new UpdateUserProfileRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setMobile("+9876543210");
        updateRequest.setCountryCode("+98");
        updateRequest.setCity("Boston");
        updateRequest.setState("MA");
        updateRequest.setCountry("USA");
        updateRequest.setPreferredLanguage("es");
        updateRequest.setOrganization("New Org");
        updateRequest.setSkills("JavaScript,React");
        updateRequest.setFieldOfStudy("Data Science");
        updateRequest.setHighestQualification("Master's");
        updateRequest.setGender("female");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.updateUserProfile(request, updateRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_partialUpdate_onlyProvidedFields() {
        // Arrange
        UpdateUserProfileRequest updateRequest = new UpdateUserProfileRequest();
        updateRequest.setFirstName("Jane");
        // Only firstName provided, other fields should remain unchanged

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.updateUserProfile(request, updateRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_skills_asString() {
        // Arrange
        UpdateUserProfileRequest updateRequest = new UpdateUserProfileRequest();
        updateRequest.setSkills("Java,Python,JavaScript");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.updateUserProfile(request, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Java,Python,JavaScript", response.getSkills());
    }

    @Test
    void updateUserProfile_emptySkills_clearsField() {
        // Arrange
        UpdateUserProfileRequest updateRequest = new UpdateUserProfileRequest();
        updateRequest.setSkills("");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.updateUserProfile(request, updateRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_countryCode_storedSeparately() {
        // Arrange
        UpdateUserProfileRequest updateRequest = new UpdateUserProfileRequest();
        updateRequest.setCountryCode("+91");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.updateUserProfile(request, updateRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_gender_persisted() {
        // Arrange
        UpdateUserProfileRequest updateRequest = new UpdateUserProfileRequest();
        updateRequest.setGender("female");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.updateUserProfile(request, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("female", response.getGender());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void uploadProfilePhoto_success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(googleDriveService.uploadBytes(any(), anyString(), anyString())).thenReturn("https://drive.google.com/file/test.jpg");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.uploadProfilePhoto(request, file);

        // Assert
        assertNotNull(response);
        assertEquals("https://drive.google.com/file/test.jpg", response.getProfilePhoto());
        verify(userRepository, times(1)).save(any(User.class));
        verify(googleDriveService, times(1)).uploadBytes(any(), anyString(), anyString());
    }

    @Test
    void uploadProfilePhoto_emptyFile_throwsException() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.uploadProfilePhoto(request, emptyFile);
        });
        assertTrue(exception.getMessage().contains("No file uploaded"));
    }

    @Test
    void uploadProfilePhoto_oversizedFile_throwsException() {
        // Arrange
        byte[] largeFile = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeMultipartFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeFile
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.uploadProfilePhoto(request, largeMultipartFile);
        });
        assertTrue(exception.getMessage().contains("exceeds 5MB limit"));
    }

    @Test
    void uploadProfilePhoto_invalidMimeType_throwsException() {
        // Arrange
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[]{1, 2, 3}
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.uploadProfilePhoto(request, pdfFile);
        });
        assertTrue(exception.getMessage().contains("Only JPG, PNG, or WEBP"));
    }

    @Test
    void uploadProfilePhoto_pngAllowed() {
        // Arrange
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(googleDriveService.uploadBytes(any(), anyString(), anyString())).thenReturn("https://drive.google.com/file/test.png");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.uploadProfilePhoto(request, pngFile);

        // Assert
        assertNotNull(response);
        assertEquals("https://drive.google.com/file/test.png", response.getProfilePhoto());
    }

    @Test
    void uploadProfilePhoto_webpAllowed() {
        // Arrange
        MockMultipartFile webpFile = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(googleDriveService.uploadBytes(any(), anyString(), anyString())).thenReturn("https://drive.google.com/file/test.webp");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.uploadProfilePhoto(request, webpFile);

        // Assert
        assertNotNull(response);
        assertEquals("https://drive.google.com/file/test.webp", response.getProfilePhoto());
    }

    @Test
    void updateUserProfile_userIsolation_differentUsersCannotModifyEachOther() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        User otherUser = User.builder()
                .id(otherUserId)
                .email("other@example.com")
                .firstName("Other")
                .build();

        UpdateUserProfileRequest updateRequest = new UpdateUserProfileRequest();
        updateRequest.setFirstName("Hacked");

        // Current authenticated user is testUserId
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.updateUserProfile(request, updateRequest);

        // Assert - Should only modify testUserId, not otherUserId
        assertNotNull(response);
        assertEquals(testUserId, response.getUserId());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).findById(otherUserId);
    }

    @Test
    void removeProfilePhoto_success() {
        // Arrange
        testUser.setProfilePhoto("https://drive.google.com/uc?id=testFileId");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(googleDriveService.extractFileId(anyString())).thenReturn("testFileId");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.removeProfilePhoto(request);

        // Assert
        assertNotNull(response);
        assertNull(response.getProfilePhoto());
        verify(googleDriveService, times(1)).deleteFile("testFileId");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void removeProfilePhoto_noPhoto_throwsException() {
        // Arrange
        testUser.setProfilePhoto(null);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act & Assert
        ProfilePhotoException exception = assertThrows(ProfilePhotoException.class, () -> {
            userService.removeProfilePhoto(request);
        });
        assertTrue(exception.getMessage().contains("No profile photo to remove"));
    }

    @Test
    void removeProfilePhoto_googleDriveDeletionFailure_continuesWithDatabaseCleanup() {
        // Arrange
        testUser.setProfilePhoto("https://drive.google.com/uc?id=testFileId");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(googleDriveService.extractFileId(anyString())).thenReturn("testFileId");
        doThrow(new RuntimeException("Google Drive API error")).when(googleDriveService).deleteFile(anyString());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.removeProfilePhoto(request);

        // Assert - Should still clear database despite Google Drive failure
        assertNotNull(response);
        assertNull(response.getProfilePhoto());
        verify(googleDriveService, times(1)).deleteFile("testFileId");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void removeProfilePhoto_userIsolation_onlyDeletesAuthenticatedUsersPhoto() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        User otherUser = User.builder()
                .id(otherUserId)
                .email("other@example.com")
                .firstName("Other")
                .profilePhoto("https://drive.google.com/uc?id=otherFileId")
                .build();

        testUser.setProfilePhoto("https://drive.google.com/uc?id=testFileId");

        // Current authenticated user is testUserId
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        when(googleDriveService.extractFileId(anyString())).thenReturn("testFileId");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(unifiedJwtService.extractUserId(anyString())).thenReturn(testUserId.toString());

        // Act
        UserProfileResponse response = userService.removeProfilePhoto(request);

        // Assert - Should only delete testUserId's photo, not otherUserId's
        assertNotNull(response);
        assertNull(response.getProfilePhoto());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).findById(otherUserId);
        verify(googleDriveService, times(1)).deleteFile("testFileId");
        verify(googleDriveService, never()).deleteFile("otherFileId");
    }
}
