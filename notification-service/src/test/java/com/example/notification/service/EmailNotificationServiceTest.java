package com.example.notification.service;

import com.cyberlearnix.commonlibs.dto.UserLoginEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SecurityVerificationTrackingService trackingService;

    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        when(trackingService.generateSignedVerificationUrl(any(), any()))
                .thenReturn("https://cyberlearnix.com/api/v1/security/verify-activity?token=test_signed_token_abc123");

        emailNotificationService = new EmailNotificationService(
                mailSender,
                new DefaultResourceLoader(),
                trackingService,
                "security@cyberlearnix.com",
                "CyberLearnix LMS",
                "https://cyberlearnix.com"
        );
        emailNotificationService.initTemplate();
    }

    @Test
    @DisplayName("Should render 2FA security email with real IP, tokenized time-limited CTA, and supporting line")
    void shouldRenderTwoFactorSecurityEmailCorrectly() {
        UserLoginEvent event = new UserLoginEvent(
                UUID.randomUUID(), "USER_LOGIN", UUID.randomUUID(),
                "student@cyberlearnix.com", "Alex Rivera",
                LocalDateTime.of(2026, 9, 8, 15, 30),
                "127.0.0.1", // loopback IP → must be normalized
                "Chrome on MacOS", "Chrome", "macOS",
                "San Francisco, CA, US", true
        );

        String html = emailNotificationService.renderEmailHtml(event);

        assertNotNull(html);

        // 1. Branding & Dark theme
        assertTrue(html.contains("CyberLearnix LMS"));
        assertTrue(html.contains("#0d0f1b"));

        // 2. 2FA copy & mandatory supporting line
        assertTrue(html.contains("Two-Step Verification adds an extra layer of security by requiring a second verification step during login."));
        assertTrue(html.contains("Two-Step Verification Protected"));

        // 3. Normalized IP (must NOT expose raw loopback address)
        assertFalse(html.contains(">127.0.0.1<"));
        assertFalse(html.contains(">localhost<"));
        assertTrue(html.contains("Gateway"));

        // 4. User-friendly Device and Browser
        assertTrue(html.contains("Apple Mac"));
        assertTrue(html.contains("Google Chrome"));

        // 5. HTTPS, tokenized CTA link (no dummy URLs like example.com)
        assertFalse(html.contains("example.com"));
        assertTrue(html.contains("https://cyberlearnix.com/api/v1/security/verify-activity?token=test_signed_token_abc123"));
        assertTrue(html.contains("Verify Activity"));

        // 6. Dynamic Risk Awareness badge for new device
        assertTrue(html.contains("Unrecognized Device or Location"));
    }

    @Test
    @DisplayName("Should include relative time context in the login timestamp")
    void shouldIncludeRelativeTimeContext() {
        UserLoginEvent event = new UserLoginEvent(
                UUID.randomUUID(), "USER_LOGIN", UUID.randomUUID(),
                "student@cyberlearnix.com", "Jane",
                LocalDateTime.now().minusMinutes(5), // 5 minutes ago
                "203.0.113.195", "Windows 11", "Firefox", "Windows",
                "Mumbai, IN", false
        );

        String html = emailNotificationService.renderEmailHtml(event);

        assertTrue(html.contains("minutes ago") || html.contains("Just now"));
        assertTrue(html.contains("Mozilla Firefox"));
        assertTrue(html.contains("203.0.***.195"));
        assertTrue(html.contains("Unknown Location") == false || html.contains("Mumbai"));
    }

    @Test
    @DisplayName("Should handle fallback location and produce clean OS and device strings")
    void shouldHandleFallbackLocationAndCleanStrings() {
        UserLoginEvent event = new UserLoginEvent(
                UUID.randomUUID(), "USER_LOGIN", UUID.randomUUID(),
                "student@cyberlearnix.com", "Jane",
                LocalDateTime.now(), "203.0.113.195",
                "Windows 11 PC", "Firefox", "Windows",
                "UNKNOWN", false
        );

        String html = emailNotificationService.renderEmailHtml(event);

        assertTrue(html.contains("Unknown Location"));
        assertTrue(html.contains("Mozilla Firefox"));
        assertTrue(html.contains("203.0.***.195"));
        assertTrue(html.contains("Windows PC"));
    }

    @Test
    @DisplayName("Should compute relative time correctly for different offsets")
    void shouldComputeRelativeTimeCorrectly() {
        assertEquals("Just now", emailNotificationService.computeRelativeTime(LocalDateTime.now().minusSeconds(30)));
        assertTrue(emailNotificationService.computeRelativeTime(LocalDateTime.now().minusMinutes(10)).contains("minutes ago"));
        assertTrue(emailNotificationService.computeRelativeTime(LocalDateTime.now().minusHours(3)).contains("hours ago"));
        assertTrue(emailNotificationService.computeRelativeTime(LocalDateTime.now().minusDays(2)).contains("days ago"));
    }
}
