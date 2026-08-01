package com.lms.coupon_service.security;

import com.cyberlearnix.security.SharedJwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtService {

    private final SharedJwtValidator sharedJwtValidator;

    public JwtService(SharedJwtValidator sharedJwtValidator) {
        this.sharedJwtValidator = sharedJwtValidator;
    }

    public String extractUserId(String token) {
        try {
            return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor("8c4e9d2f1a7b6c5d9e3f0a1b7c8d4e5f9a2b6c1d8e7f3a4b5c9d1e6f8a2b7c3".getBytes()))
                .build()
                .parseClaimsJws(cleanToken(token))
                .getBody()
                .getSubject();
        } catch (Exception e) {
            // Fallback: parse without signature validation for testing
            try {
                String[] parts = cleanToken(token).split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.readTree(payload).get("sub").asText();
                }
            } catch (Exception ex) {
                System.out.println("Failed to extract userId: " + ex.getMessage());
            }
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor("8c4e9d2f1a7b6c5d9e3f0a1b7c8d4e5f9a2b6c1d8e7f3a4b5c9d1e6f8a2b7c3".getBytes()))
                .build()
                .parseClaimsJws(cleanToken(token))
                .getBody()
                .get("role", String.class);
        } catch (Exception e) {
            // Fallback: parse without signature validation for testing
            try {
                String[] parts = cleanToken(token).split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.readTree(payload).get("role").asText();
                }
            } catch (Exception ex) {
                System.out.println("Failed to extract role: " + ex.getMessage());
            }
            return null;
        }
    }

    public String extractAssignedService(String token) {
        try {
            return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor("8c4e9d2f1a7b6c5d9e3f0a1b7c8d4e5f9a2b6c1d8e7f3a4b5c9d1e6f8a2b7c3".getBytes()))
                .build()
                .parseClaimsJws(cleanToken(token))
                .getBody()
                .get("assignedService", String.class);
        } catch (Exception e) {
            // Fallback: parse without signature validation for testing
            try {
                String[] parts = cleanToken(token).split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.readTree(payload).get("assignedService").asText();
                }
            } catch (Exception ex) {
                System.out.println("Failed to extract assignedService: " + ex.getMessage());
            }
            return null;
        }
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