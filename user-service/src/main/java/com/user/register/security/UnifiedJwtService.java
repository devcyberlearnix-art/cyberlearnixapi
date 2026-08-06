package com.user.register.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UnifiedJwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer:cyberlearnix}")
    private String issuer;

    @Value("${jwt.audience:cyberlearnix-clients}")
    private String audience;

    @Value("${jwt.access-token.expiration-minutes:15}")
    private long accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token.expiration-days:30}")
    private long refreshTokenExpirationDays;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        log.debug("JWT Secret length: {}", keyBytes.length);
        log.debug("JWT Secret: {}", jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String userId, String email, String role, String adminType, String assignedService) {
        log.debug("Generating access token for userId: {}, email: {}, role: {}", userId, email, role);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("adminType", adminType != null ? adminType : "NONE");
        claims.put("assignedService", assignedService != null ? assignedService : "NONE");
        claims.put("type", "access");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + TimeUnit.MINUTES.toMillis(accessTokenExpirationMinutes));

        var builder = Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);
        
        if (issuer != null && !issuer.isBlank()) {
            builder.setIssuer(issuer);
        }
        if (audience != null && !audience.isBlank()) {
            builder.setAudience(audience);
        }
        
        String token = builder.compact();
        log.debug("Generated access token successfully");
        return token;
    }

    public String generateRefreshToken(String userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "refresh");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + TimeUnit.DAYS.toMillis(refreshTokenExpirationDays));

        var builder = Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256);
        
        if (issuer != null && !issuer.isBlank()) {
            builder.setIssuer(issuer);
        }
        if (audience != null && !audience.isBlank()) {
            builder.setAudience(audience);
        }
        
        return builder.compact();
    }

    public Claims extractClaims(String token) {
        var parserBuilder = Jwts.parserBuilder()
                .setSigningKey(getSigningKey());
        
        if (issuer != null && !issuer.isBlank()) {
            parserBuilder.requireIssuer(issuer);
        }
        if (audience != null && !audience.isBlank()) {
            parserBuilder.requireAudience(audience);
        }
        
        return parserBuilder.build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String extractAdminType(String token) {
        return extractClaims(token).get("adminType", String.class);
    }

    public String extractAssignedService(String token) {
        return extractClaims(token).get("assignedService", String.class);
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}
