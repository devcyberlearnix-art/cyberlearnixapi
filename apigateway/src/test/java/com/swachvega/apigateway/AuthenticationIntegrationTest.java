package com.swachvega.apigateway;

import com.swachvega.apigateway.security.SimpleJwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private SimpleJwtTokenProvider jwtTokenProvider;

    @Test
    void testJwtTokenGeneration() {
        // Test JWT token generation
        String accessToken = jwtTokenProvider.generateAccessToken(
                "user123",
                "testuser",
                "session123",
                "CUSTOMER"
        );
        
        assertNotNull(accessToken);
        assertTrue(accessToken.contains("."));
        
        // Test token validation
        Map<String, Object> claims = jwtTokenProvider.validateAccessToken(accessToken).block();
        
        assertNotNull(claims);
        assertEquals("user123", claims.get("sub"));
        assertEquals("testuser", claims.get("username"));
        assertEquals("session123", claims.get("sessionId"));
        assertEquals("CUSTOMER", claims.get("role"));
    }

    @Test
    void testRefreshTokenGeneration() {
        // Test refresh token generation
        String refreshToken = jwtTokenProvider.generateRefreshToken("user123", "session123");
        
        assertNotNull(refreshToken);
        assertTrue(refreshToken.contains("."));
        
        // Test refresh token validation
        Map<String, Object> claims = jwtTokenProvider.validateRefreshToken(refreshToken).block();
        
        assertNotNull(claims);
        assertEquals("user123", claims.get("sub"));
        assertEquals("session123", claims.get("sessionId"));
        assertEquals("refresh", claims.get("type"));
    }

    @Test
    void testTokenExpiration() {
        // Test token expiration (this would need tokens with very short expiration)
        String accessToken = jwtTokenProvider.generateAccessToken(
                "user123",
                "testuser",
                "session123",
                "CUSTOMER"
        );
        
        // Verify token is valid
        Map<String, Object> claims = jwtTokenProvider.validateAccessToken(accessToken).block();
        assertNotNull(claims);
        
        // For a proper expiration test, you'd need to mock the time or use tokens with very short expiration
        // This is a basic structure test
    }

    @Test
    void testTokenExtractionFromHeader() {
        String authHeader = "Bearer abc123def456";
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        
        assertEquals("abc123def456", token);
        
        // Test null header
        assertNull(jwtTokenProvider.extractTokenFromHeader(null));
        
        // Test invalid header
        assertNull(jwtTokenProvider.extractTokenFromHeader("InvalidHeader"));
    }
}
