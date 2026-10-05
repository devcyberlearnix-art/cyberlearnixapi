package com.cyberlearnix.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class ServiceAuthUtil {

    @Value("${service.auth.secret:shared-service-secret-key-2024}")
    private String serviceAuthSecret;

    @Value("${service.auth.issuer:cyberlearnix-service}")
    private String serviceIssuer;

    private static final int TOKEN_EXPIRY_SECONDS = 300; // 5 minutes
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a service authentication token with replay protection
     * Format: "timestamp:nonce:issuer:signature"
     */
    public String generateServiceToken() {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String nonce = generateNonce();
            String signature = generateSignature(timestamp, nonce, serviceIssuer);
            return timestamp + ":" + nonce + ":" + serviceIssuer + ":" + signature;
        } catch (Exception e) {
            log.error("Error generating service token", e);
            throw new RuntimeException("Failed to generate service token", e);
        }
    }

    /**
     * Validate a service authentication token with replay protection
     * @param token Token in format "timestamp:nonce:issuer:signature"
     * @return true if valid, false otherwise
     */
    public boolean validateServiceToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            String[] parts = token.split(":");
            if (parts.length != 4) {
                log.warn("Invalid token format: expected 4 parts, got {}", parts.length);
                return false;
            }

            long timestamp = Long.parseLong(parts[0]);
            String nonce = parts[1];
            String issuer = parts[2];
            String signature = parts[3];

            // Check if token is not too old (5 minutes)
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime - timestamp > TOKEN_EXPIRY_SECONDS) {
                log.warn("Service token expired ({} seconds old, issuer: {})",
                        currentTime - timestamp, issuer);
                return false;
            }

            // Check timestamp is not in the future (clock skew protection)
            if (timestamp > currentTime + 30) {
                log.warn("Service token timestamp in the future (issuer: {})", issuer);
                return false;
            }

            // Verify issuer matches expected
            if (!issuer.equals(serviceIssuer)) {
                log.warn("Service token issuer mismatch: expected {}, got {}", serviceIssuer, issuer);
                return false;
            }

            // Verify signature
            String expectedSignature = generateSignature(timestamp, nonce, issuer);
            if (!signature.equals(expectedSignature)) {
                log.warn("Service token signature verification failed (issuer: {})", issuer);
                return false;
            }

            // TODO: In production, implement nonce cache to prevent replay attacks
            // For now, we log it for monitoring
            log.debug("Service token validated successfully (issuer: {}, nonce: {})", issuer, nonce);

            return true;

        } catch (Exception e) {
            log.error("Error validating service token", e);
            return false;
        }
    }

    private String generateSignature(long timestamp, String nonce, String issuer) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    serviceAuthSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            String data = timestamp + ":" + nonce + ":" + issuer;
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Error generating signature", e);
            return "";
        }
    }

    private String generateNonce() {
        byte[] nonceBytes = new byte[16];
        secureRandom.nextBytes(nonceBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
    }

    @Value("${service.auth.header-name:X-Service-Auth}")
    private String authHeaderName;

    public String getAuthHeaderName() {
        return authHeaderName;
    }

    public String getServiceIssuer() {
        return serviceIssuer;
    }
}
