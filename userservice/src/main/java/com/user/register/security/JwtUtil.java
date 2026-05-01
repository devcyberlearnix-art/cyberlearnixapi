package com.user.register.security;

import com.user.register.entity.User;
import com.user.register.service.TokenBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final TokenBlacklistService blacklistService;

    @Value("${jwt.access-token.secret:myVerySecretKeyForAccessTokenThatIsAtLeast32CharactersLongForHS256Algorithm}")
    private String accessSecret;

    @Value("${jwt.refresh-token.secret:myVerySecretKeyForRefreshTokenThatIsAtLeast32CharactersLongForHS256Algorithm}")
    private String refreshSecret;

    @Value("${jwt.issuer:cyberlearnix}")
    private String issuer;

    @Value("${jwt.audience:cyberlearnix-clients}")
    private String audience;

    private static final long ACCESS_TOKEN_EXPIRATION = 15L * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 30L * 24 * 60 * 60 * 1000; // 30 days


    // Generate secret key
    private SecretKey getSecretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


    // ================= TOKEN GENERATION =================
    private String generateToken(String subject, long expiration, String type, String role, String secret) {

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuer(issuer)
                .claim("aud", audience)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("role", role) // ✅ now comes from parameter
                .signWith(getSecretKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String userId, String role) {
        return generateToken(userId, ACCESS_TOKEN_EXPIRATION, "access", role, accessSecret);
    }

    public String generateRefreshToken(User user, String userId, String role) {
        return generateToken(userId, REFRESH_TOKEN_EXPIRATION, "refresh", role, refreshSecret);
    }

    // ================= CLAIM EXTRACTION =================

    private Claims extractAllClaims(String token, String secret) {

        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey(secret))
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String extractUserId(String token) {
        return extractAllClaims(token, accessSecret).getSubject();
    }


    public String extractTokenType(String token) {
        return extractAllClaims(token, accessSecret).get("type", String.class);
    }


    public Date extractExpiration(String token) {
        return extractAllClaims(token, accessSecret).getExpiration();
    }


    // ================= VALIDATION =================

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token, accessSecret);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }


    public String validateRefreshTokenAndGetUserId(String token) {

        try {

            Claims claims = extractAllClaims(token, refreshSecret);

            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid token type"
                );
            }

            return claims.getSubject();

        } catch (JwtException e) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token"
            );
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

    public String validateAccessTokenAndGetUserId(String token) {

        if (token == null || token.isBlank()) {
            throw new RuntimeException("JWT token is missing");
        }

        try {

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey(accessSecret))
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (!"access".equals(claims.get("type", String.class))) {
                throw new RuntimeException("Invalid JWT token type");
            }

            return claims.getSubject();

        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token");
        }
    }

    // ================= SOCIAL LOGIN TOKEN =================
    public String generateAccessTokenForSocialLogin(User user) {
        // user.getId() as subject, 15 min expiration, type "access", include role
        return generateToken(
                String.valueOf(user.getId()),       // subject
                JwtUtil.ACCESS_TOKEN_EXPIRATION,    // 15 minutes
                "access",                           // token type
                user.getRole().name(),              // role
                accessSecret                        // secret
        );
    }

}