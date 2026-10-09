package com.example.notification.controller;

import com.example.notification.service.NotificationTrustedDeviceService;
import com.example.notification.service.SecurityVerificationTrackingService;
import com.example.notification.service.SecurityVerificationTrackingService.VerificationTokenPayload;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Real backend API for time-limited 2FA security review and action tracking.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/security")
@RequiredArgsConstructor
public class SecurityVerificationController {

    private final SecurityVerificationTrackingService trackingService;
    private final NotificationTrustedDeviceService trustedDeviceService;

    /**
     * Handles CTA link clicks from email/SMS/Push.
     * Validates token signature, enforces 15-minute expiration, and logs click tracking.
     */
    @GetMapping("/verify-activity")
    public ResponseEntity<?> verifyActivity(
            @RequestParam("token") String token,
            HttpServletRequest request) {

        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        VerificationTokenPayload payload = trackingService.validateToken(token);

        if (!payload.isValid()) {
            HttpStatus status = payload.isExpired() ? HttpStatus.GONE : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of(
                    "status", "ERROR",
                    "code", payload.isExpired() ? "LINK_EXPIRED" : "INVALID_TOKEN",
                    "message", payload.getErrorMessage(),
                    "support", "If you suspect unauthorized activity, please sign in to https://cyberlearnix.com/account/security immediately."
            ));
        }

        // Track user CTA click
        trackingService.trackAction(payload.getEventId(), payload.getUserId(), "CTA_LINK_CLICKED", clientIp, userAgent);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Security link verified. Please confirm whether this sign-in was authorized by you.",
                "eventId", payload.getEventId().toString(),
                "userId", payload.getUserId().toString(),
                "token", token,
                "options", Map.of(
                        "confirmSafeEndpoint", "/api/v1/security/confirm-activity",
                        "reportCompromisedEndpoint", "/api/v1/security/report-compromised"
                )
        ));
    }

    /**
     * User confirms: "Yes, this was me."
     * Marks the device as trusted for 30 days and suppresses repeated spam alerts.
     */
    @PostMapping("/confirm-activity")
    public ResponseEntity<?> confirmActivity(
            @RequestParam("token") String token,
            @RequestParam(value = "device", required = false, defaultValue = "Verified Device") String device,
            @RequestParam(value = "os", required = false, defaultValue = "Secure OS") String os,
            HttpServletRequest request) {

        VerificationTokenPayload payload = trackingService.validateToken(token);
        if (!payload.isValid()) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", payload.getErrorMessage()));
        }

        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Mark device as trusted
        trustedDeviceService.markAsTrusted(payload.getUserId(), device, os);

        // Track audit
        trackingService.trackAction(payload.getEventId(), payload.getUserId(), "VERIFIED_SAFE_BY_USER", clientIp, userAgent);

        return ResponseEntity.ok(Map.of(
                "status", "CONFIRMED",
                "message", "Thank you. This device has been recorded as trusted for your account. No further action is required."
        ));
    }

    /**
     * User reports: "No, this wasn't me."
     * Logs urgent compromised alert, prepares session revocation.
     */
    @PostMapping("/report-compromised")
    public ResponseEntity<?> reportCompromised(
            @RequestParam("token") String token,
            HttpServletRequest request) {

        VerificationTokenPayload payload = trackingService.validateToken(token);
        if (!payload.isValid()) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", payload.getErrorMessage()));
        }

        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Track urgent compromised incident
        trackingService.trackAction(payload.getEventId(), payload.getUserId(), "REPORTED_UNAUTHORIZED_ACTIVITY", clientIp, userAgent);
        log.warn("🚨 [CRITICAL ALERT] User {} flagged event {} as unauthorized from IP {}!",
                payload.getUserId(), payload.getEventId(), clientIp);

        return ResponseEntity.ok(Map.of(
                "status", "ALERT_RECEIVED",
                "message", "Your account protection has been initiated. Active sessions are being invalidated.",
                "nextSteps", "Please reset your password immediately at https://cyberlearnix.com/auth/reset-password"
        ));
    }
}
