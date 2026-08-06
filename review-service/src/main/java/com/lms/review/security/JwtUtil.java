package com.lms.review.security;

import com.cyberlearnix.security.SharedJwtValidator;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    private final SharedJwtValidator sharedJwtValidator;

    public JwtUtil(SharedJwtValidator sharedJwtValidator) {
        this.sharedJwtValidator = sharedJwtValidator;
    }

    public Claims extractAllClaims(String token) {
        return sharedJwtValidator.extractAllClaims(cleanToken(token));
    }

    public UUID extractUserId(Claims claims) {
        if (claims.get("userId") != null) {
            return UUID.fromString(String.valueOf(claims.get("userId")));
        }
        return UUID.fromString(claims.getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (rolesClaim == null) {
            rolesClaim = claims.get("role");
        }
        if (rolesClaim instanceof Collection<?> collection) {
            List<String> roles = new ArrayList<>();
            for (Object role : collection) {
                roles.add(normalizeRole(String.valueOf(role)));
            }
            return roles;
        }
        if (rolesClaim instanceof String role) {
            return List.of(normalizeRole(role));
        }
        return List.of("STUDENT");
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "STUDENT";
        }
        String upper = role.toUpperCase();
        if ("USER".equals(upper)) {
            return "STUDENT";
        }
        if (upper.contains("ADMIN")) {
            return "ADMIN";
        }
        return upper;
    }

    public boolean isTokenValid(String token) {
        return sharedJwtValidator.isTokenValid(cleanToken(token));
    }

    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
