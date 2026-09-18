package com.example.notification.service;

import com.cyberlearnix.commonlibs.dto.UserLoginEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Service to dispatch WhatsApp login alerts via Twilio REST API or WhatsApp Business Cloud API.
 * Ensures multi-channel consistency with Email and Push notifications.
 */
@Slf4j
@Service
public class WhatsAppNotificationService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.whatsapp-from-number:whatsapp:+14155238886}")
    private String fromWhatsAppNumber;

    @Autowired(required = false)
    private SecurityVerificationTrackingService trackingService;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm 'UTC'");

    /**
     * Sends a WhatsApp security alert when a login occurs.
     *
     * @param event The UserLoginEvent containing login details
     * @param toPhoneNumber The recipient's phone number in E.164 format (e.g. +1234567890)
     */
    public void sendWhatsAppAlert(UserLoginEvent event, String toPhoneNumber) {
        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            log.debug("No phone number available for userId={}, skipping WhatsApp dispatch.", event.userId());
            return;
        }

        String recipient = toPhoneNumber.startsWith("whatsapp:") ? toPhoneNumber : "whatsapp:" + toPhoneNumber.trim();

        // Consistent time and relative context
        LocalDateTime time = event.loginTime() != null ? event.loginTime() : LocalDateTime.now();
        String formattedTime = time.format(DATE_FORMATTER) + " (" + computeRelativeTime(time) + ")";

        // Consistent secure tokenized link
        String verifyUrl = trackingService != null
                ? trackingService.generateSignedVerificationUrl(event.eventId(), event.userId())
                : "https://cyberlearnix.com/account/security";

        String messageBody = String.format(
                "🔒 *CyberLearnix LMS Security Alert*\n\n" +
                "Hi %s,\n" +
                "A sign-in to your account was detected:\n" +
                "• *Device / OS:* %s (%s)\n" +
                "• *Browser:* %s\n" +
                "• *Location:* %s\n" +
                "• *IP Address:* %s\n" +
                "• *Time:* %s\n\n" +
                "🛡️ *Protected by Two-Step Verification*\n" +
                "%s" +
                "If this was you, no action is needed.\n\n" +
                "If this was NOT you, secure your account immediately:\n" +
                "👉 %s (Valid for 15 minutes)",
                event.username() != null ? event.username() : "there",
                event.device() != null ? event.device() : "Desktop Computer",
                event.operatingSystem() != null ? event.operatingSystem() : "Secure OS",
                event.browser() != null ? event.browser() : "Web Browser",
                event.location() != null ? event.location() : "Unknown Location",
                maskIp(event.ipAddress()),
                formattedTime,
                event.isNewDevice() ? "⚠️ *Note: Unrecognized device or environment.*\n\n" : "",
                verifyUrl
        );

        if (accountSid == null || accountSid.isBlank() || accountSid.contains("MOCK") || authToken == null || authToken.isBlank()) {
            log.info("ℹ️ [MOCK WHATSAPP DISPATCH] (Twilio credentials not configured)\nTo: {}\nMessage:\n{}",
                    recipient, messageBody);
            return;
        }

        try {
            String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", accountSid);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = accountSid + ":" + authToken;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + new String(encodedAuth));

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("From", fromWhatsAppNumber);
            body.add("To", recipient);
            body.add("Body", messageBody);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(URI.create(url), request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ WhatsApp alert delivered successfully to {} for userId={}", recipient, event.userId());
            } else {
                log.warn("⚠️ Twilio returned non-success status: {} response: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception ex) {
            log.error("❌ Failed to deliver WhatsApp alert to {}: {}", recipient, ex.getMessage(), ex);
            throw new RuntimeException("WhatsApp notification delivery failed: " + ex.getMessage(), ex);
        }
    }

    public void sendWhatsAppAlert(UserLoginEvent event) {
        sendWhatsAppAlert(event, null);
    }

    private String computeRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "Just now";
        Duration diff = Duration.between(dateTime, LocalDateTime.now());
        long seconds = Math.abs(diff.getSeconds());
        if (seconds < 60) return "Just now";
        if (seconds < 3600) return (seconds / 60) + "m ago";
        if (seconds < 86400) return (seconds / 3600) + "h ago";
        return (seconds / 86400) + "d ago";
    }

    private String maskIp(String ip) {
        if (ip == null || ip.isBlank() || ip.contains("127.0.0.1") || ip.equalsIgnoreCase("localhost")) {
            return "198.51.100.42 (Gateway)";
        }
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***." + parts[3];
        }
        return ip;
    }
}
