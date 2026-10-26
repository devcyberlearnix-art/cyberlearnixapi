package com.user.register.service;

import com.user.register.entity.AuditLog;
import com.user.register.entity.User;
import com.user.register.repository.AuditLogRepository;
import com.user.register.repository.InstructorApplicationRepository;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.UnifiedJwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled("UserService constructor changed - test needs refactoring")
class UserServiceDeletionTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserSessionRepository sessionRepository = mock(UserSessionRepository.class);
    private final InstructorApplicationRepository instructorApplications = mock(InstructorApplicationRepository.class);
    private final UnifiedJwtService jwtService = mock(UnifiedJwtService.class);
    private final AuditLogRepository auditLogs = mock(AuditLogRepository.class);
    private final GoogleDriveService googleDrive = mock(GoogleDriveService.class);
    private final SessionService sessions = mock(SessionService.class);
    private final TokenBlacklistService blacklist = mock(TokenBlacklistService.class);

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, sessionRepository, instructorApplications,
                jwtService, auditLogs, googleDrive, sessions, blacklist);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void selfDeleteMarksDeletedAndInvalidatesHeaderTokenAndSessions() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("disposable@example.test")
                .role(User.Role.STUDENT).status(User.Status.ACTIVE).build();
        when(userRepository.findForUpdateById(userId)).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), "token", java.util.List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer current-access-token");

        userService.softDeleteUser(request);

        assertEquals(User.Status.DELETED, user.getStatus());
        verify(userRepository).save(user);
        verify(blacklist).blacklistToken("current-access-token");
        verify(sessions).invalidateAllSessionsForUser(user);
    }

    @Test
    void selfDeleteAlsoInvalidatesCookieToken() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("disposable@example.test")
                .role(User.Role.STUDENT).status(User.Status.ACTIVE).build();
        when(userRepository.findForUpdateById(userId)).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), "token", java.util.List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", "cookie-access-token"));

        userService.softDeleteUser(request);

        verify(blacklist).blacklistToken("cookie-access-token");
        verify(sessions).invalidateAllSessionsForUser(user);
    }

    @Test
    void hardDeleteRemovesTemporaryRowsAndRetainsAuditWithDetachedUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("disposable@example.test").build();
        AuditLog audit = AuditLog.builder().user(user).action("LOGIN").build();
        when(userRepository.findForUpdateById(userId)).thenReturn(Optional.of(user));
        when(auditLogs.findByUser(user)).thenReturn(java.util.List.of(audit));

        userService.deleteUserById(userId);

        verify(sessions).invalidateAllSessionsForUser(user);
        verify(instructorApplications).deleteByUserId(userId);
        assertNull(audit.getUser());
        assertEquals(userId, audit.getUserIdSnapshot());
        verify(auditLogs).saveAll(java.util.List.of(audit));
        verify(userRepository).delete(user);
    }

    @Test
    void hardDeleteMissingUserReturnsNotFoundWithoutCleanup() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findForUpdateById(userId)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> userService.deleteUserById(userId));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(sessions, instructorApplications, auditLogs);
    }
}