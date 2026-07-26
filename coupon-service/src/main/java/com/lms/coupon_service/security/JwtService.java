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
        return sharedJwtValidator.extractUserId(cleanToken(token));
    }

    public String extractRole(String token) {
        return sharedJwtValidator.extractRole(cleanToken(token));
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