package com.user.register.security;

import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.service.TokenBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final TokenBlacklistService blacklistService;
    private final SecretKey secretKey;
    private final String issuer;
    private final String audience;

    private static final long ACCESS_TOKEN_EXPIRATION = 15L * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 30L * 24 * 60 * 60 * 1000; // 30 days

    public JwtUtil(
            TokenBlacklistService blacklistService,
            @Value("${jwt.secret:${jwt.access-token.secret:myVerySecretKeyForAccessTokenThatIsAtLeast32CharactersLongForHS256Algorithm}}") String secret,
            @Value("${jwt.issuer:cyberlearnix}") String issuer,
            @Value("${jwt.audience:cyberlearnix-clients}") String audience
    ) {
        this.blacklistService = blacklistService;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
    }

    // Generate secret key
    private SecretKey getSecretKey() {
        return this.secretKey;
    }

    // ================= TOKEN GENERATION =================
    private String generateToken(String subject, long expiration, String type, String role) {

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuer(this.issuer)
                .setAudience(this.audience)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("role", role) // ✅ now comes from parameter
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String userId, String role) {
        return generateToken(userId, ACCESS_TOKEN_EXPIRATION, "access", normalizeJwtRole(role));
    }

    public String generateRefreshToken(User user, String userId, String role) {
        return generateToken(userId, REFRESH_TOKEN_EXPIRATION, "refresh", normalizeJwtRole(role));
    }

    private String normalizeJwtRole(String role) {
        if (role == null)
            return "USER";
        if ("STUDENT".equalsIgnoreCase(role))
            return "USER";
        return role.toUpperCase();
    }

    // ================= CLAIM EXTRACTION =================

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .requireIssuer(this.issuer)
                .requireAudience(this.audience)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // ================= VALIDATION =================

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String validateRefreshTokenAndGetUserId(String token) {

        try {

            Claims claims = extractAllClaims(token);

            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid token type");
            }

            return claims.getSubject();

        } catch (JwtException e) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token");
        }
    }

    // ================= RANDOM TOKEN GENERATION =================

    public String generateStrongAccessToken(Long userId, int byteLength) {

        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[byteLength];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String generateStrongRefreshToken(Long userId, int byteLength) {

        SecureRandom random = new SecureRandom();

        byte[] bytes = new byte[byteLength];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Claims parseAccessTokenClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "JWT token is missing");
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .requireIssuer(this.issuer)
                    .requireAudience(this.audience)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            if (!"access".equals(claims.get("type", String.class))) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Invalid token type. User access token required");
            }
            return claims;
        } catch (JwtException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid or expired user access token");
        }
    }

    public void requireUserAccessToken(String token) {
        Claims claims = parseAccessTokenClaims(token);
        String role = claims.get("role", String.class);
        if (role == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Token missing role claim");
        }
        String normalized = role.toUpperCase();
        if (!"USER".equals(normalized) && !"STUDENT".equals(normalized)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Instructor apply requires User access token. Current token role: " + role
                            + ". Login as user and pass that access token in Authorization Bearer header.");
        }
    }

    public UUID resolveUserIdFromAccessToken(String token, UserRepository userRepository) {
        String subject = parseAccessTokenClaims(token).getSubject();
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            return userRepository.findByEmail(subject)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found for token"))
                    .getId();
        }
    }

    public String validateAccessTokenAndGetUserId(String token) {
        return parseAccessTokenClaims(token).getSubject();
    }

    // ================= SOCIAL LOGIN TOKEN =================
    public String generateAccessTokenForSocialLogin(User user) {
        // user.getId() as subject, 15 min expiration, type "access", include role
        return generateToken(
                String.valueOf(user.getId()), // subject
                JwtUtil.ACCESS_TOKEN_EXPIRATION, // 15 minutes
                "access", // token type
                user.getRole().name() // role
        );
    }

    public String generateResetToken(String userId) {
        return Jwts.builder()
                .setSubject(userId) // 🔥 IMPORTANT
                .claim("type", "reset_password")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

}