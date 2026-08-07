package com.lms.courseservice.security;

import com.cyberlearnix.security.SharedJwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    private final SharedJwtValidator sharedJwtValidator;

    public JwtUtil(SharedJwtValidator sharedJwtValidator) {
        this.sharedJwtValidator = sharedJwtValidator;
    }

    public String extractUsername(String token) {
        return sharedJwtValidator.extractUserId(cleanToken(token));
    }

    public String extractRole(String token) {
        return sharedJwtValidator.extractRole(cleanToken(token));
    }

    public boolean validateToken(String token) {
        return sharedJwtValidator.isTokenValid(cleanToken(token));
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(sharedJwtValidator.extractUserId(cleanToken(token)));
    }

    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    public static String toSpringSecurityRole(String role) {
        if (role == null || role.isBlank()) {
            return "STUDENT";
        }
        String upper = role.toUpperCase();
        if (upper.startsWith("ROLE_")) {
            upper = upper.substring(5);
        }
        
        if ("USER".equals(upper) || "STUDENT".equals(upper)) {
            return "STUDENT";
        }
        if (upper.contains("MAIN_ADMIN")) {
            return "MAIN_ADMIN";
        }
        if (upper.contains("SUB_ADMIN")) {
            return "SUB_ADMIN";
        }
        if (upper.contains("INSTRUCTOR")) {
            return "INSTRUCTOR";
        }
        if (upper.contains("ADMIN")) {
            return "MAIN_ADMIN"; // Fallback for general ADMIN
        }
        return upper;
    }
}
