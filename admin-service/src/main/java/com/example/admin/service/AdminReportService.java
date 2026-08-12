package com.example.admin.service;

import com.example.admin.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${user.service.url:http://localhost:8091}")
    private String userService;

    @Value("${course-service.url:http://localhost:8083}")
    private String courseService;

    @Value("${order-service.url:http://localhost:8084}")
    private String orderService;

    @Value("${payment-service.url:http://localhost:8085}")
    private String paymentService;

    @Value("${settings-service.url:http://localhost:8089}")
    private String settingsService;

    // ✅ COMMON HEADER HANDLER
    private HttpEntity<Void> getEntity(String token) {
        HttpHeaders headers = new HttpHeaders();

        String effectiveToken = token == null || token.isBlank()
                ? jwtService.generateServiceToken("ADMIN")
                : token;

        if (!effectiveToken.startsWith("Bearer ")) {
            effectiveToken = "Bearer " + effectiveToken;
        }

        headers.set("Authorization", effectiveToken);
        return new HttpEntity<>(headers);
    }

    // ===== USERS REPORT =====
    public Map<String, Object> getUserReport(String token) {
        Map<String, Object> response = restTemplate.exchange(
            userService + "/api/v1/users/stats",
            HttpMethod.GET,
            getEntity(token),
            new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();
        return extractData(response);
    }

    // ===== COURSES REPORT =====
    public Map<String, Object> getCourseReport(String token) {
        return restTemplate.exchange(
                courseService + "/api/v1/courses/stats",
                HttpMethod.GET,
                getEntity(token),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractData(Map<String, Object> response) {
        if (response == null || !(response.get("data") instanceof Map<?, ?> data)) {
            return Map.of();
        }
        return (Map<String, Object>) data;
    }

    // ===== REVENUE REPORT =====
    public Map<String, Object> getRevenueReport(String token) {
        try {
            return restTemplate.exchange(
                    paymentService + "/payments/revenue",
                    HttpMethod.GET,
                    getEntity(token),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();
        } catch (Exception e) {
            return Map.of(
                    "error", "Failed to fetch revenue report",
                    "details", e.getMessage()
            );
        }
    }

    // ===== ORDER REPORT =====
    public Map<String, Object> getOrderReport(String token) {
        try {
            return restTemplate.exchange(
                    orderService + "/orders/analytics",
                    HttpMethod.GET,
                    getEntity(token),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();
        } catch (Exception e) {
            return Map.of(
                    "error", "Failed to fetch order analytics",
                    "details", e.getMessage()
            );
        }
    }

    // ===== SETTINGS =====
    public boolean updatePlatformSettings(Map<String, Object> data) {
        try {
            restTemplate.exchange(settingsService + "/settings/platform", HttpMethod.PUT, getEntity(null), Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updatePaymentSettings(Map<String, Object> data) {
        try {
            restTemplate.exchange(settingsService + "/settings/payment", HttpMethod.PUT, getEntity(null), Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateNotificationSettings(Map<String, Object> data) {
        try {
            restTemplate.exchange(settingsService + "/settings/notifications", HttpMethod.PUT, getEntity(null), Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}