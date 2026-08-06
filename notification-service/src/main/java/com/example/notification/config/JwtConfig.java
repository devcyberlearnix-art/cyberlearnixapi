package com.example.notification.config;

import com.cyberlearnix.security.SharedJwtValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public SharedJwtValidator sharedJwtValidator(
            @Value("${jwt.secret:8c4e9d2f1a7b6c5d9e3f0a1b7c8d4e5f9a2b6c1d8e7f3a4b5c9d1e6f8a2b7c3}") String secret,
            @Value("${jwt.issuer:cyberlearnix}") String issuer,
            @Value("${jwt.audience:cyberlearnix-clients}") String audience) {
        return new SharedJwtValidator(secret, issuer, audience);
    }
}
