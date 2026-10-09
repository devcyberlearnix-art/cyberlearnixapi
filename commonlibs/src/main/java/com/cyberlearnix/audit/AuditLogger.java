package com.cyberlearnix.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class AuditLogger {

    public void logEvent(String eventType, String userId, String service, String action,
                        String resource, String details, String ipAddress, String status) {
        AuditEvent event = AuditEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .userId(userId)
                .service(service)
                .action(action)
                .resource(resource)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();

        log.info("AUDIT_EVENT: {}", event);

        // In production, this would be sent to a centralized audit service or database
        // For now, we log it which can be collected by log aggregation tools
    }

    public void logSuccess(String eventType, String userId, String service, String action,
                          String resource, String details, String ipAddress) {
        logEvent(eventType, userId, service, action, resource, details, ipAddress, "SUCCESS");
    }

    public void logFailure(String eventType, String userId, String service, String action,
                          String resource, String details, String ipAddress) {
        logEvent(eventType, userId, service, action, resource, details, ipAddress, "FAILURE");
    }
}
