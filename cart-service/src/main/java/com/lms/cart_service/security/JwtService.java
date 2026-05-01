package com.lms.cart_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // 1. Injected from application.properties to match Gateway/User Service
    @Value("${jwt.access-token.secret}")
    private String secretKey;

    public String extractUserId(String token) {
        return extractClaim(cleanToken(token), Claims::getSubject);
    }

    public String extractRole(String token) {
        // 2. Changed from "roles" to "role" to match your actual JWT payload
        return extractClaim(cleanToken(token), claims -> claims.get("role", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            final Date expiration = extractClaim(cleanToken(token), Claims::getExpiration);
            return expiration != null && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignInKey() {
        // 3. Using StandardCharsets to ensure consistent byte conversion
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}