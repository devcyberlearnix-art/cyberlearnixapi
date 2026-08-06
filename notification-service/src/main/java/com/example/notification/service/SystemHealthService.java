package com.example.notification.service;

import com.example.notification.dto.SystemHealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemHealthService {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;

    // 🔥 Dynamic URLs
    @Value("${health.services.userService}")
    private String userServiceUrl;

    @Value("${health.services.courseService:}")
    private String courseServiceUrl;

    @Value("${health.timeout}")
    private int timeout;

    public SystemHealthResponse getHealthStatus() {

        Map<String, String> checks = new HashMap<>();

        boolean dbUp = checkDatabase();
        checks.put("database", dbUp ? "UP" : "DOWN");

        boolean userServiceUp = checkExternalService("userService", userServiceUrl, checks);

        boolean courseServiceUp = true;
        if (courseServiceUrl != null && !courseServiceUrl.isEmpty()) {
            courseServiceUp = checkExternalService("courseService", courseServiceUrl, checks);
        }

        String overallStatus = dbUp ? "UP" : "DOWN";

        return SystemHealthResponse.builder()
                .timestamp(Instant.now())
                .status(overallStatus)
                .service("notification-service")
                .version("1.0.0")
                .uptime(getFormattedUptime())
                .checks(checks)
                .build();
    }

    // 🔹 DB CHECK
    private boolean checkDatabase() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 🔥 GENERIC EXTERNAL SERVICE CHECK
    private boolean checkExternalService(String name, String url, Map<String, String> checks) {

        long start = System.currentTimeMillis();

        try {
            restTemplate.getForObject(url, String.class);

            long latency = System.currentTimeMillis() - start;

            checks.put(name, "UP (" + latency + "ms)");

            return true;

        } catch (Exception e) {

            long latency = System.currentTimeMillis() - start;

            checks.put(name, "DOWN (" + latency + "ms)");

            return false;
        }
    }

    // 🔹 UPTIME FORMAT
    private String getFormattedUptime() {

        long uptimeMillis =
                ManagementFactory.getRuntimeMXBean().getUptime();

        Duration duration = Duration.ofMillis(uptimeMillis);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return hours + "h " + minutes + "m " + seconds + "s";
    }
}