package com.swachvega.apigateway.security;

import com.swachvega.apigateway.config.JwtProperties;
import com.swachvega.apigateway.util.JwtUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Production-ready JWT Token Provider using JJWT library
 */
@Component
@Slf4j
public class SimpleJwtTokenProvider {

    private final SecretKey accessTokenSecret;
    private final SecretKey refreshTokenSecret;
    private final int accessTokenExpirationMinutes;
    private final int refreshTokenExpirationDays;
    private final JwtParser accessTokenParser;
    private final JwtParser refreshTokenParser;
    private final String issuer;
    private final String audience;

    public SimpleJwtTokenProvider(
            @Value("${jwt.access-token.secret:myVerySecretKeyForAccessTokenThatIsAtLeast32CharactersLongForHS256Algorithm}") String accessSecret,
            @Value("${jwt.refresh-token.secret:myVerySecretKeyForRefreshTokenThatIsAtLeast32CharactersLongForHS256Algorithm}") String refreshSecret,
            @Value("${jwt.access-token.expiration-minutes:15}") int accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token.expiration-days:30}") int refreshTokenExpirationDays,
            @Value("${jwt.issuer:swachvega}") String issuer,
            @Value("${jwt.audience:swachvega-clients}") String audience) {
        
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        
        // Create secret keys (support both raw and Base64-encoded secrets)
        byte[] accessBytes = decodeIfBase64(accessSecret);
        byte[] refreshBytes = decodeIfBase64(refreshSecret);
        this.accessTokenSecret = Keys.hmacShaKeyFor(accessBytes);
        this.refreshTokenSecret = Keys.hmacShaKeyFor(refreshBytes);
        
        // Create parsers
        this.accessTokenParser = Jwts.parser()
                .verifyWith(accessTokenSecret)
                .build();
        
        this.refreshTokenParser = Jwts.parser()
                .verifyWith(refreshTokenSecret)
                .build();
        
        this.issuer = issuer;
        this.audience = audience;
        
        log.info("JwtTokenProvider initialized - Access token expiry: {} minutes, Refresh token expiry: {} days", 
                accessTokenExpirationMinutes, refreshTokenExpirationDays);
    }

    private byte[] decodeIfBase64(String secret) {
        if (secret == null) return new byte[0];
        String s = secret.trim();
        // Heuristic: Base64 strings are commonly length % 4 == 0 and contain only base64 charset
        boolean looksBase64 = s.length() % 4 == 0 && s.matches("[A-Za-z0-9+/=]+");
        try {
            if (looksBase64) {
                byte[] decoded = Decoders.BASE64.decode(s);
                // If decoding yields very short key, fallback to raw bytes
                if (decoded != null && decoded.length >= 32) {
                    return decoded;
                }
            }
        } catch (Exception ignored) { }
        return s.getBytes();
    }

    public String generateAccessToken(String userId, String username, String sessionId, String role, 
                                     String email, String fullName, String phoneNumber, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenExpirationMinutes * 60L);
        
        // Create comprehensive user claims using utility
        Map<String, Object> userClaims = JwtUtils.createUserClaims(
            userId, username, email, fullName, phoneNumber, role, sessionId, additionalClaims
        );
        
        // Build JWT with all claims
        JwtBuilder builder = Jwts.builder()
                .subject(userId)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .signWith(accessTokenSecret);
        
        // Add all user claims
        for (Map.Entry<String, Object> entry : userClaims.entrySet()) {
            if (!entry.getKey().equals("sub")) { // subject is already set
                builder.claim(entry.getKey(), entry.getValue());
            }
        }
        // Ensure token type
        builder.claim("type", "access");
        
        String token = builder.compact();
        log.debug("Generated access token for user {} with expiry {} and claims: {}", 
                userId, expiration, userClaims.keySet());
        return token;
    }
    
    // Overloaded method for backward compatibility
    public String generateAccessToken(String userId, String username, String sessionId, String role) {
        return generateAccessToken(userId, username, sessionId, role, null, null, null, null);
    }

    public String generateRefreshToken(String userId, String sessionId) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(refreshTokenExpirationDays * 24 * 60 * 60L);
        
        String token = Jwts.builder()
                .subject(userId)
                .claim("sessionId", sessionId)
                .claim("type", "refresh")
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .signWith(refreshTokenSecret)
                .compact();
        
        log.debug("Generated refresh token for user {} with expiry {}", userId, expiration);
        return token;
    }

    public Mono<Map<String, Object>> validateAccessToken(String token) {
        log.debug("Validating access token: {}...", token != null ? token.substring(0, Math.min(20, token.length())) : "null");
        
        return Mono.fromCallable(() -> {
            try {
                Jws<Claims> jws = accessTokenParser.parseSignedClaims(token);
                Claims claims = jws.getPayload();
                
                // Check token type
                String type = claims.get("type", String.class);
                if (!"access".equals(type)) {
                    log.warn("Invalid token type: expected=access, actual={}", type);
                    throw new JwtException("Invalid token type");
                }
                // Check issuer and audience
                String iss = claims.getIssuer();
                if (iss == null || !iss.equals(issuer)) {
                    log.warn("Invalid issuer: expected={}, actual={}", issuer, iss);
                    throw new JwtException("Invalid issuer");
                }
                Object audClaim = claims.get("aud");
                if (audClaim == null || !audClaim.toString().contains(audience)) {
                    log.warn("Invalid audience: expected contains={}, actual={}", audience, audClaim);
                    throw new JwtException("Invalid audience");
                }
                
                // Convert claims to map for backward compatibility
                Map<String, Object> claimsMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    claimsMap.put(entry.getKey(), entry.getValue());
                }
                // Explicitly include standard registered claims not always present in entrySet
                if (claims.getSubject() != null) {
                    claimsMap.put("sub", claims.getSubject());
                }
                if (claims.getIssuer() != null) {
                    claimsMap.put("iss", claims.getIssuer());
                }
                Object aud = claims.get("aud");
                if (aud != null) {
                    claimsMap.put("aud", aud);
                }
                
                log.debug("Token validation successful for user: {}", claims.getSubject());
                return claimsMap;
                
            } catch (ExpiredJwtException e) {
                log.warn("Token expired: {}", e.getMessage());
                throw new JwtException("Token expired", e);
            } catch (UnsupportedJwtException e) {
                log.warn("Unsupported token: {}", e.getMessage());
                throw new JwtException("Unsupported token", e);
            } catch (MalformedJwtException e) {
                log.warn("Malformed token: {}", e.getMessage());
                throw new JwtException("Malformed token", e);
            } catch (JwtException e) {
                log.warn("JWT validation failed: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Unexpected error during token validation: {}", e.getMessage(), e);
                throw new JwtException("Token validation failed", e);
            }
        })
        .doOnError(ex -> log.error("Token validation failed: {}", ex.getMessage()))
        .onErrorResume(ex -> {
            log.debug("Returning empty due to validation error: {}", ex.getMessage());
            return Mono.empty();
        });
    }

    public Mono<Map<String, Object>> validateRefreshToken(String token) {
        log.debug("Validating refresh token: {}...", token != null ? token.substring(0, Math.min(20, token.length())) : "null");
        
        return Mono.fromCallable(() -> {
            try {
                Jws<Claims> jws = refreshTokenParser.parseSignedClaims(token);
                Claims claims = jws.getPayload();
                
                // Check token type
                String type = claims.get("type", String.class);
                if (!"refresh".equals(type)) {
                    log.warn("Invalid token type: expected=refresh, actual={}", type);
                    throw new JwtException("Invalid token type");
                }
                // Check issuer and audience
                String iss = claims.getIssuer();
                if (iss == null || !iss.equals(issuer)) {
                    log.warn("Invalid issuer for refresh: expected={}, actual={}", issuer, iss);
                    throw new JwtException("Invalid issuer");
                }
                Object audClaim = claims.get("aud");
                if (audClaim == null || !audClaim.toString().contains(audience)) {
                    log.warn("Invalid audience for refresh: expected contains={}, actual={}", audience, audClaim);
                    throw new JwtException("Invalid audience");
                }
                
                // Convert claims to map for backward compatibility
                Map<String, Object> claimsMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    claimsMap.put(entry.getKey(), entry.getValue());
                }
                
                log.debug("Refresh token validation successful for user: {}", claims.getSubject());
                return claimsMap;
                
            } catch (ExpiredJwtException e) {
                log.warn("Refresh token expired: {}", e.getMessage());
                throw new JwtException("Refresh token expired", e);
            } catch (Exception e) {
                log.error("Refresh token validation failed: {}", e.getMessage(), e);
                throw new JwtException("Refresh token validation failed", e);
            }
        })
        .doOnError(ex -> log.error("Refresh token validation failed: {}", ex.getMessage()))
        .onErrorResume(ex -> {
            log.debug("Returning empty due to refresh token validation error: {}", ex.getMessage());
            return Mono.empty();
        });
    }

    public String extractTokenFromHeader(String authHeader) {
        log.debug("Extracting token from header: {}", authHeader != null ? "Bearer ***" : "null");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                log.warn("Empty token after Bearer prefix");
                return null;
            }
            log.debug("Token extracted successfully");
            return token;
        }
        
        log.warn("Invalid or missing Bearer token in header");
        return null;
    }

    public int getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60L;
    }
}
