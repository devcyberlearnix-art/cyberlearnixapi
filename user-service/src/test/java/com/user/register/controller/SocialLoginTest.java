package com.user.register.controller;

import com.cyberlearnix.commonlibs.dto.UserLoginEvent;
import com.user.register.dto.unified.LoginResponse;
import com.user.register.dto.unified.OAuthProviderResponse;
import com.user.register.dto.unified.SocialLoginRequest;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.security.UnifiedJwtService;
import com.user.register.service.SessionService;
import com.user.register.service.UnifiedAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.user.register.util.DynamicDeviceAndLocationResolver;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SocialLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, UserLoginEvent> kafkaTemplate;

    @Mock
    private UnifiedJwtService unifiedJwtService;

    @Mock
    private SessionService sessionService;

    @Mock
    private HttpServletRequest httpRequest;

    private UnifiedAuthenticationService unifiedAuthenticationService;
    private UnifiedAuthenticationController unifiedAuthenticationController;

    @BeforeEach
    void setUp() {
        unifiedAuthenticationService = new UnifiedAuthenticationService(
                userRepository,
                kafkaTemplate,
                null,
                unifiedJwtService,
                null,
                null,
                null,
                null,
                sessionService,
                new DynamicDeviceAndLocationResolver()
        );

        unifiedAuthenticationController = new UnifiedAuthenticationController(unifiedAuthenticationService);

        when(unifiedJwtService.generateAccessToken(any(), any(), any(), any(), any()))
                .thenReturn("mock-access-token");
        when(unifiedJwtService.generateRefreshToken(any(), any(), any()))
                .thenReturn("mock-refresh-token");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0 TestBrowser");
    }

    @Test
    void testGoogleSocialLogin_NewUser_Success() {
        // Arrange
        String email = "googleuser@gmail.com";
        SocialLoginRequest request = SocialLoginRequest.builder()
                .provider("google")
                .email(email)
                .firstName("Google")
                .lastName("User")
                .profilePhoto("https://lh3.googleusercontent.com/photo.jpg")
                .providerId("google-123456")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(userId);
            return saved;
        });

        // Act
        ResponseEntity<LoginResponse> response = unifiedAuthenticationController.socialLogin(request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("googleuser@gmail.com", response.getBody().getUser().getEmail());
        assertEquals("STUDENT", response.getBody().getUser().getRole());
        assertEquals("mock-access-token", response.getBody().getAuthentication().getAccessToken());
        assertEquals("mock-refresh-token", response.getBody().getAuthentication().getRefreshToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(sessionService, times(1)).createSession(any(User.class), eq(httpRequest), eq("mock-access-token"), eq("mock-refresh-token"));
    }

    @Test
    void testGithubSocialLogin_ExistingUser_Success() {
        // Arrange
        String email = "octocat@github.com";
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(User.Role.STUDENT)
                .status(User.Status.ACTIVE)
                .build();

        SocialLoginRequest request = SocialLoginRequest.builder()
                .provider("github")
                .email(email)
                .firstName("Octo")
                .lastName("Cat")
                .providerId("gh-999")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<LoginResponse> response = unifiedAuthenticationController.socialLoginWithProvider("github", request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(email, response.getBody().getUser().getEmail());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testLinkedinSocialLogin_OAuthContinue_Success() {
        // Arrange
        String email = "pro@linkedin.com";
        SocialLoginRequest request = SocialLoginRequest.builder()
                .provider("linkedin")
                .email(email)
                .firstName("John")
                .lastName("Dev")
                .providerId("li-555")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        ResponseEntity<LoginResponse> response = unifiedAuthenticationController.oauthContinue(request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(email, response.getBody().getUser().getEmail());
    }

    @Test
    void testSocialLogin_InvalidProvider_ThrowsBadRequest() {
        // Arrange
        SocialLoginRequest request = SocialLoginRequest.builder()
                .provider("facebook")
                .email("fb@facebook.com")
                .build();

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                unifiedAuthenticationController.socialLogin(request, httpRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Unsupported OAuth provider"));
    }

    @Test
    void testSocialLogin_LockedUser_ThrowsLocked() {
        // Arrange
        String email = "locked@google.com";
        User lockedUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .status(User.Status.LOCKED)
                .build();

        SocialLoginRequest request = SocialLoginRequest.builder()
                .provider("google")
                .email(email)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(lockedUser));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                unifiedAuthenticationController.socialLogin(request, httpRequest)
        );
        assertEquals(HttpStatus.LOCKED, ex.getStatusCode());
    }

    @Test
    void testGetOAuthProviders_ReturnsConfiguredProviders() {
        // Act
        ResponseEntity<OAuthProviderResponse> response = unifiedAuthenticationController.getOAuthProviders();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getProviders().size());
        assertTrue(response.getBody().getProviders().stream().anyMatch(p -> "google".equals(p.getId())));
        assertTrue(response.getBody().getProviders().stream().anyMatch(p -> "github".equals(p.getId())));
        assertTrue(response.getBody().getProviders().stream().anyMatch(p -> "linkedin".equals(p.getId())));
    }
}
