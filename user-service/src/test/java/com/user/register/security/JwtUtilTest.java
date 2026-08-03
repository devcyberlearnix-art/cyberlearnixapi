package com.user.register.security;

import com.user.register.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class JwtUtilTest {

    private static final String TEST_SECRET = "myVerySecretKeyForAccessTokenThatIsAtLeast32CharactersLongForHS256Algorithm";
    private static final String TEST_ISSUER = "cyberlearnix";
    private static final String TEST_AUDIENCE = "cyberlearnix-clients";

    @Test
    void shouldValidateTokenUsingConfiguredSecretAndIssuer() {
        TokenBlacklistService blacklistService = mock(TokenBlacklistService.class);
        JwtUtil jwtUtil = new JwtUtil(blacklistService, TEST_SECRET, TEST_ISSUER, TEST_AUDIENCE);

        String token = jwtUtil.generateAccessToken("user-123", "USER");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("USER", jwtUtil.extractRole(token));
    }
}
