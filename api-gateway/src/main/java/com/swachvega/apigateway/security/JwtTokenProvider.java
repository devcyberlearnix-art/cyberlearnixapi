package com.swachvega.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey accessTokenSecret;
    private final SecretKey refreshTokenSecret;
    private final int accessTokenExpirationMinutes;
    private final int refreshTokenExpirationDays;

    public JwtTokenProvider(
            @Value("${jwt.access-token.secret:${jwt.secret:myVerySecretKeyForAccessTokenThatIsAtLeast32CharactersLongForHS256Algorithm}}") String accessSecret,
            @Value("${jwt.refresh-token.secret:${jwt.secret:myVerySecretKeyForRefreshTokenThatIsAtLeast32CharactersLongForHS256Algorithm}}") String refreshSecret,
            @Value("${jwt.access-token.expiration-minutes:15}") int accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token.expiration-days:30}") int refreshTokenExpirationDays) {

        this.accessTokenSecret = Keys.hmacShaKeyFor(accessSecret.getBytes());
        this.refreshTokenSecret = Keys.hmacShaKeyFor(refreshSecret.getBytes());
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public String generateAccessToken(String userId, String username, String sessionId, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("username", username)
                .claim("sessionId", sessionId)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(accessTokenSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId, String sessionId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("sessionId", sessionId)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(refreshTokenSecret, Jwts.SIG.HS256)
                .compact();
    }

    public Mono<Claims> validateAccessToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                return Jwts.parser()
                        .verifyWith(accessTokenSecret)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception e) {
                throw new SecurityException("Invalid access token", e);
            }
        });
    }

    public Mono<Claims> validateRefreshToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                return Jwts.parser()
                        .verifyWith(refreshTokenSecret)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception e) {
                throw new SecurityException("Invalid refresh token", e);
            }
        });
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public int getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60L;
    }
}
