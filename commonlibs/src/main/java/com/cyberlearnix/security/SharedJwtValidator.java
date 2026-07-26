package com.cyberlearnix.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Shared JWT Validator for all microservices.
 * This class provides unified JWT validation logic across the entire system.
 * Only the User Service should generate JWTs; all other services should only validate them.
 */
@Slf4j
public class SharedJwtValidator {

    private final SecretKey secretKey;
    private final String issuer;
    private final String audience;

    public SharedJwtValidator(String secret, String issuer, String audience) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
    }

    /**
     * Validate JWT token and extract claims.
     * 
     * @param token JWT token
     * @return Claims object containing token data
     * @throws JwtException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        try {
            var parserBuilder = Jwts.parserBuilder()
                    .setSigningKey(secretKey);
            
            if (issuer != null && !issuer.isBlank()) {
                parserBuilder.requireIssuer(issuer);
            }
            
            if (audience != null && !audience.isBlank()) {
                parserBuilder.requireAudience(audience);
            }
            
            return parserBuilder.build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            throw new JwtException("Token expired, please login again");
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw new JwtException("Invalid token");
        }
    }

    /**
     * Extract user ID from JWT token.
     * 
     * @param token JWT token
     * @return User ID as string
     */
    public String extractUserId(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extract email from JWT token.
     * 
     * @param token JWT token
     * @return Email address
     */
    public String extractEmail(String token) {
        return validateToken(token).get("email", String.class);
    }

    /**
     * Extract role from JWT token.
     * 
     * @param token JWT token
     * @return Role (STUDENT, INSTRUCTOR, MAIN_ADMIN, SUB_ADMIN)
     */
    public String extractRole(String token) {
        return validateToken(token).get("role", String.class);
    }

    /**
     * Extract admin type from JWT token.
     * 
     * @param token JWT token
     * @return Admin type (MAIN_ADMIN, SUB_ADMIN, NONE)
     */
    public String extractAdminType(String token) {
        return validateToken(token).get("adminType", String.class);
    }

    /**
     * Extract assigned service from JWT token.
     * 
     * @param token JWT token
     * @return Assigned service
     */
    public String extractAssignedService(String token) {
        return validateToken(token).get("assignedService", String.class);
    }

    /**
     * Extract token type from JWT token.
     * 
     * @param token JWT token
     * @return Token type (access, refresh)
     */
    public String extractTokenType(String token) {
        return validateToken(token).get("type", String.class);
    }

    /**
     * Check if token is valid without throwing exception.
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired.
     * 
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extract all claims from JWT token.
     * 
     * @param token JWT token
     * @return All claims
     */
    public Claims extractAllClaims(String token) {
        return validateToken(token);
    }
}
