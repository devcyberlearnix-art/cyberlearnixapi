package com.user.register.service;

import com.user.register.entity.UserSession;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.util.DynamicDeviceAndLocationResolver;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceRotationTest {

    private final UserSessionRepository sessions = mock(UserSessionRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TokenBlacklistService blacklist = mock(TokenBlacklistService.class);
    private final DynamicDeviceAndLocationResolver deviceResolver = mock(DynamicDeviceAndLocationResolver.class);
    private final SessionService service = new SessionService(mock(JwtUtil.class), sessions, users, blacklist, deviceResolver);

    @Test
    void rotatesPersistedTokensAndInvalidatesBothOldTokens() {
        UserSession session = new UserSession();
        session.setAccessToken("old-access");
        session.setRefreshToken("old-refresh");
        when(sessions.findForUpdateByRefreshToken("old-refresh")).thenReturn(Optional.of(session));

        service.rotateTokens("old-refresh", "new-access", "new-refresh");

        verify(blacklist).blacklistToken("old-access");
        verify(blacklist).blacklistToken("old-refresh");
        assertEquals("new-access", session.getAccessToken());
        assertEquals("new-refresh", session.getRefreshToken());
        verify(sessions).save(session);
    }

    @Test
    void missingRefreshSessionIsRejected() {
        when(sessions.findForUpdateByRefreshToken("missing")).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.rotateTokens("missing", "new-access", "new-refresh"));

        assertEquals(401, error.getStatusCode().value());
        verify(blacklist, never()).blacklistToken(anyString());
    }
}
