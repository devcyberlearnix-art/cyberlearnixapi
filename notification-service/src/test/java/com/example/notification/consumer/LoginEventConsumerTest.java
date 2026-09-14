package com.example.notification.consumer;

import com.cyberlearnix.commonlibs.dto.UserLoginEvent;
import com.example.notification.repository.DeviceTokenRepository;
import com.example.notification.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginEventConsumerTest {

    @Mock private EmailNotificationService emailNotificationService;
    @Mock private AuditLogService auditLogService;
    @Mock private KafkaIdempotencyService idempotencyService;
    @Mock private FirebasePushService firebasePushService;
    @Mock private DeviceTokenRepository deviceTokenRepository;
    @Mock private WhatsAppNotificationService whatsAppNotificationService;
    @Mock private NotificationRateLimiterService rateLimiterService;
    @Mock private NotificationTrustedDeviceService trustedDeviceService;
    @Mock private SecurityVerificationTrackingService trackingService;
    @Mock private Acknowledgment acknowledgment;

    @InjectMocks
    private LoginEventConsumer loginEventConsumer;

    private UserLoginEvent testEvent;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testEvent = new UserLoginEvent(
                UUID.randomUUID(), "USER_LOGIN", userId,
                "test@example.com", "John Doe",
                LocalDateTime.now(), "192.168.1.100",
                "Chrome on Windows 11", "Chrome", "Windows 11",
                "Hyderabad, IN", true
        );
    }

    @Test
    @DisplayName("Should process new device login: run rate limit, trusted device check, all channels, then commit offset")
    void shouldProcessNewDeviceLoginSuccessfully() {
        when(idempotencyService.isFirstTime(testEvent.eventId())).thenReturn(true);
        when(trustedDeviceService.isTrusted(userId, testEvent.device(), testEvent.operatingSystem())).thenReturn(false);
        when(rateLimiterService.allowAlert(userId)).thenReturn(true);
        when(deviceTokenRepository.findTokensByUserIds(List.of(userId.toString()))).thenReturn(List.of("fcm-token-abc"));
        when(trackingService.generateSignedVerificationUrl(any(), any())).thenReturn("https://cyberlearnix.com/api/v1/security/verify-activity?token=signed_token");

        loginEventConsumer.consume(testEvent, 0, 100L, acknowledgment);

        verify(idempotencyService).isFirstTime(testEvent.eventId());
        verify(trustedDeviceService).isTrusted(userId, testEvent.device(), testEvent.operatingSystem());
        verify(rateLimiterService).allowAlert(userId);
        verify(auditLogService).logLoginEvent(testEvent);
        verify(emailNotificationService).sendNewLoginAlert(testEvent);
        verify(firebasePushService).sendToMultipleDevices(eq(List.of("fcm-token-abc")), anyString(), anyString(), anyMap());
        verify(whatsAppNotificationService).sendWhatsAppAlert(testEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should skip duplicate event via idempotency check and immediately acknowledge")
    void shouldSkipDuplicateEvent() {
        when(idempotencyService.isFirstTime(testEvent.eventId())).thenReturn(false);

        loginEventConsumer.consume(testEvent, 1, 101L, acknowledgment);

        verify(idempotencyService).isFirstTime(testEvent.eventId());
        verify(rateLimiterService, never()).allowAlert(any());
        verify(auditLogService, never()).logLoginEvent(any());
        verify(emailNotificationService, never()).sendNewLoginAlert(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should suppress alerts for trusted devices and only log audit")
    void shouldSuppressAlertsForTrustedDevice() {
        when(idempotencyService.isFirstTime(testEvent.eventId())).thenReturn(true);
        when(trustedDeviceService.isTrusted(userId, testEvent.device(), testEvent.operatingSystem())).thenReturn(true);

        loginEventConsumer.consume(testEvent, 0, 102L, acknowledgment);

        verify(auditLogService).logLoginEvent(testEvent);
        verify(rateLimiterService, never()).allowAlert(any());
        verify(emailNotificationService, never()).sendNewLoginAlert(any());
        verify(whatsAppNotificationService, never()).sendWhatsAppAlert(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should suppress alerts when rate limit is exceeded")
    void shouldSuppressAlertsWhenRateLimitExceeded() {
        when(idempotencyService.isFirstTime(testEvent.eventId())).thenReturn(true);
        when(trustedDeviceService.isTrusted(userId, testEvent.device(), testEvent.operatingSystem())).thenReturn(false);
        when(rateLimiterService.allowAlert(userId)).thenReturn(false);

        loginEventConsumer.consume(testEvent, 1, 103L, acknowledgment);

        verify(auditLogService).logLoginEvent(testEvent);
        verify(emailNotificationService, never()).sendNewLoginAlert(any());
        verify(whatsAppNotificationService, never()).sendWhatsAppAlert(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should rethrow exceptions to allow DefaultErrorHandler retry and DLQ routing")
    void shouldRethrowForKafkaRetryAndDlq() {
        when(idempotencyService.isFirstTime(testEvent.eventId())).thenReturn(true);
        when(trustedDeviceService.isTrusted(userId, testEvent.device(), testEvent.operatingSystem())).thenReturn(false);
        when(rateLimiterService.allowAlert(userId)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(auditLogService).logLoginEvent(testEvent);

        assertThrows(RuntimeException.class, () ->
                loginEventConsumer.consume(testEvent, 2, 104L, acknowledgment));

        verify(acknowledgment, never()).acknowledge();
    }
}
