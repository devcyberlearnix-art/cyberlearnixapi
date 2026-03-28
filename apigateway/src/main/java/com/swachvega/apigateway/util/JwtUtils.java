package com.swachvega.apigateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Utility class for production-level operations
 */
@Component
@Slf4j
public class JwtUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create comprehensive user claims for JWT token
     */
    public static Map<String, Object> createUserClaims(String userId, String username, String email, 
                                                      String fullName, String phoneNumber, String role,
                                                      String sessionId, Map<String, Object> additionalClaims) {
        Map<String, Object> claims = new HashMap<>();
        
        // Standard claims
        claims.put("sub", userId);
        claims.put("username", username);
        claims.put("email", email);
        claims.put("fullName", fullName);
        claims.put("phoneNumber", phoneNumber);
        claims.put("role", role);
        claims.put("sessionId", sessionId);
        claims.put("type", "access");
        
        // Token metadata
        claims.put("iat", Instant.now().getEpochSecond());
        claims.put("jti", UUID.randomUUID().toString());
        
        // User permissions/features (can be extended)
        claims.put("permissions", getUserPermissions(role));
        claims.put("features", getUserFeatures(role));
        
        // Add any additional claims
        if (additionalClaims != null && !additionalClaims.isEmpty()) {
            claims.putAll(additionalClaims);
        }
        
        return claims;
    }

    /**
     * Extract user information from JWT claims
     */
    public static Map<String, Object> extractUserInfo(Claims claims) {
        Map<String, Object> userInfo = new HashMap<>();
        
        userInfo.put("userId", claims.getSubject());
        userInfo.put("username", claims.get("username"));
        userInfo.put("email", claims.get("email"));
        userInfo.put("fullName", claims.get("fullName"));
        userInfo.put("phoneNumber", claims.get("phoneNumber"));
        userInfo.put("role", claims.get("role"));
        userInfo.put("sessionId", claims.get("sessionId"));
        userInfo.put("permissions", claims.get("permissions"));
        userInfo.put("features", claims.get("features"));
        
        // Token metadata
        userInfo.put("tokenId", claims.getId());
        userInfo.put("issuedAt", claims.getIssuedAt());
        userInfo.put("expiresAt", claims.getExpiration());
        
        return userInfo;
    }

    /**
     * Get user permissions based on role
     */
    private static String[] getUserPermissions(String role) {
        switch (role.toUpperCase()) {
            case "ADMIN":
                return new String[]{"READ", "WRITE", "DELETE", "MANAGE_USERS", "MANAGE_PRODUCTS", "MANAGE_ORDERS", "VIEW_ANALYTICS"};
            case "STORE_MANAGER":
                return new String[]{"READ", "WRITE", "MANAGE_PRODUCTS", "MANAGE_ORDERS", "VIEW_STORE_ANALYTICS"};
            case "DELIVERY_PARTNER":
                return new String[]{"READ", "UPDATE_DELIVERY_STATUS", "VIEW_ASSIGNED_ORDERS"};
            case "CONSUMER":
                return new String[]{"READ", "WRITE", "PLACE_ORDERS", "VIEW_ORDER_HISTORY", "MANAGE_PROFILE"};
            default:
                return new String[]{"READ"};
        }
    }

    /**
     * Get user features based on role
     */
    private static String[] getUserFeatures(String role) {
        switch (role.toUpperCase()) {
            case "ADMIN":
                return new String[]{"ADMIN_DASHBOARD", "USER_MANAGEMENT", "PRODUCT_MANAGEMENT", "ORDER_MANAGEMENT", "ANALYTICS", "SETTINGS"};
            case "STORE_MANAGER":
                return new String[]{"STORE_DASHBOARD", "INVENTORY_MANAGEMENT", "ORDER_MANAGEMENT", "STORE_ANALYTICS"};
            case "DELIVERY_PARTNER":
                return new String[]{"DELIVERY_DASHBOARD", "ORDER_TRACKING", "DELIVERY_HISTORY", "EARNINGS"};
            case "CONSUMER":
                return new String[]{"SHOPPING", "ORDER_TRACKING", "FAVORITES", "REVIEWS", "WALLET", "PROFILE"};
            default:
                return new String[]{"BASIC"};
        }
    }

    /**
     * Validate token claims
     */
    public static boolean validateTokenClaims(Claims claims) {
        // Check if token is expired
        if (claims.getExpiration().before(new Date())) {
            log.warn("Token is expired");
            return false;
        }
        
        // Check if token has required claims
        if (claims.getSubject() == null || claims.get("sessionId") == null) {
            log.warn("Token missing required claims");
            return false;
        }
        
        // Check token type
        String tokenType = (String) claims.get("type");
        if (tokenType == null || (!tokenType.equals("access") && !tokenType.equals("refresh"))) {
            log.warn("Invalid token type: {}", tokenType);
            return false;
        }
        
        return true;
    }

    /**
     * Create device info claims
     */
    public static Map<String, Object> createDeviceInfoClaims(String deviceId, String deviceType, 
                                                            String osVersion, String appVersion,
                                                            String ipAddress, String userAgent) {
        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("deviceId", deviceId);
        deviceInfo.put("deviceType", deviceType);
        deviceInfo.put("osVersion", osVersion);
        deviceInfo.put("appVersion", appVersion);
        deviceInfo.put("ipAddress", ipAddress);
        deviceInfo.put("userAgent", userAgent);
        deviceInfo.put("lastUsed", Instant.now().getEpochSecond());
        
        return Map.of("deviceInfo", deviceInfo);
    }

    /**
     * Convert object to JSON string safely
     */
    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON", e);
            return "{}";
        }
    }

    /**
     * Parse JSON string to Map safely
     */
    public static Map<String, Object> parseJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to Map", e);
            return new HashMap<>();
        }
    }

    /**
     * Check if token is close to expiry (for proactive refresh)
     */
    public static boolean isTokenCloseToExpiry(Claims claims, int bufferMinutes) {
        Date expiration = claims.getExpiration();
        long currentTime = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        long bufferTime = bufferMinutes * 60 * 1000L; // Convert minutes to milliseconds
        
        return (expirationTime - currentTime) <= bufferTime;
    }
}
