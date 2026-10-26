package com.user.register.controller;

import com.cyberlearnix.security.ServiceAuthUtil;
import com.user.register.security.UnifiedJwtService;
import com.user.register.service.TokenBlacklistService;
import com.user.register.service.UserService;
import com.user.register.service.GoogleDriveService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserDeletionSecurityTest {

    @Mock private UserService userService;
    @Mock private UnifiedJwtService jwtService;
    @Mock private ServiceAuthUtil serviceAuthUtil;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private GoogleDriveService googleDriveService;
    @Mock private Claims claims;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, jwtService, serviceAuthUtil, tokenBlacklistService, googleDriveService);
        when(serviceAuthUtil.getAuthHeaderName()).thenReturn("X-Service-Auth");
        when(serviceAuthUtil.validateServiceToken("service-token")).thenReturn(true);
        when(jwtService.validateToken(anyString())).thenReturn(true);
        when(jwtService.isTokenExpired(anyString())).thenReturn(false);
        when(jwtService.extractClaims(anyString())).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("access");
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);
    }

    @Test
    void unauthenticatedDeleteIsRejected() {
        ResponseEntity<?> response = controller.deleteUser(UUID.randomUUID(), new MockHttpServletRequest());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void regularUserCannotDeleteAnotherUserById() {
        when(jwtService.extractRole("user-token")).thenReturn("STUDENT");
        ResponseEntity<?> response = controller.deleteUser(UUID.randomUUID(), authorizedRequest("user-token"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void authorizedAdminWithSignedJwtAndServiceProofCanDelete() {
        when(jwtService.extractRole("admin-token")).thenReturn("MAIN_ADMIN");
        UUID userId = UUID.randomUUID();
        ResponseEntity<?> response = controller.deleteUser(userId, authorizedRequest("admin-token"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deleteUserById(userId);
    }

    private MockHttpServletRequest authorizedRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("X-Service-Auth", "service-token");
        return request;
    }
}
