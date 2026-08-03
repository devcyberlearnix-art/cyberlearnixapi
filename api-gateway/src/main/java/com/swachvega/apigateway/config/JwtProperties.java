package com.swachvega.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties for Production
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();

    @Data
    public static class AccessToken {
        private String secret = "myVerySecretKeyForAccessTokenThatIsAtLeast32CharactersLongForHS256Algorithm";
        private int expirationMinutes = 15;
        private String algorithm = "HS256";
        private String issuer = "cyberlearnix";
        private String audience = "cyberlearnix-clients";
    }

    @Data
    public static class RefreshToken {
        private String secret = "myVerySecretKeyForRefreshTokenThatIsAtLeast32CharactersLongForHS256Algorithm";
        private int expirationDays = 30;
        private String algorithm = "HS256";
        private String issuer = "cyberlearnix";
        private String audience = "cyberlearnix-clients";
    }
    
    // Security settings
    @Data
    public static class Security {
        private boolean enableClockSkew = true;
        private int clockSkewSeconds = 30;
        private boolean enableTokenBlacklist = true;
        private int tokenBlacklistCacheSize = 10000;
        private boolean enableRateLimiting = true;
        private int rateLimitRequestsPerMinute = 100;
    }
    
    private Security security = new Security();
}
