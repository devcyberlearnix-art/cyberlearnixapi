package com.example.instructorservice.security;

import com.cyberlearnix.security.SharedJwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

    private final SharedJwtValidator sharedJwtValidator;

    public JwtUtil(SharedJwtValidator sharedJwtValidator) {
        this.sharedJwtValidator = sharedJwtValidator;
    }

    public String extractUserId(String token) {
        return sharedJwtValidator.extractUserId(cleanToken(token));
    }

    public String extractRole(String token) {
        return sharedJwtValidator.extractRole(cleanToken(token));
    }

    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}