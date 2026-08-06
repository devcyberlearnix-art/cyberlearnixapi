package com.lms.courseservice;

import com.lms.courseservice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateTokenIncludesIssuerAndAudience() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateToken(userId, "MAIN_ADMIN");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(userId, jwtUtil.extractUserId(token));
        assertEquals("MAIN_ADMIN", jwtUtil.extractRole(token));
    }
}
