package com.user.register.security;

import com.user.register.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class JwtUtilTest {

    @Test
    void shouldValidateTokenUsingConfiguredSecretAndIssuer() {
        TokenBlacklistService blacklistService = mock(TokenBlacklistService.class);
        JwtUtil jwtUtil = new JwtUtil(blacklistService);

        String token = jwtUtil.generateAccessToken("user-123", "USER");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("USER", jwtUtil.extractRole(token));
    }
}
