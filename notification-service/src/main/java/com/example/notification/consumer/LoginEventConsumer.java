package com.example.notification.consumer;

import com.cyberlearnix.commonlibs.dto.UserLoginEvent;
import com.example.notification.repository.DeviceTokenRepository;
import com.example.notification.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginEventConsumer {

    private final EmailNotificationService emailNotificationService;
    private final AuditLogService auditLogService;
    private final KafkaIdempotencyService idempotencyService;
    private final FirebasePushService firebasePushService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final NotificationRateLimiterService rateLimiterService;
    private final NotificationTrustedDeviceService trustedDeviceService;
    private final SecurityVerificationTrackingService trackingService;

    @KafkaListener(
            topics = "${app.kafka.topic.user-login:user-login-topic}",
            groupId = "${spring.kafka.consumer.group-id:notification-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload UserLoginEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Processing UserLoginEvent [eventId={}, userId={}, partition={}, offset={}]",
                event.eventId(), event.userId(), partition, offset);

        try {
            // 1. Idempotency Check: guard against duplicate redelivery
            if (!idempotencyService.isFirstTime(event.eventId())) {
                log.info("Skipping already processed event: eventId={}", event.eventId());
                acknowledgment.acknowledge();
                return;
            }

            // 2. Suppress alerts for trusted devices verified by user
            if (trustedDeviceService.isTrusted(event.userId(), event.device(), event.operatingSystem())) {
                log.info("Device is verified and trusted for userId={}. Suppressing alert spam [eventId={}].",
                        event.userId(), event.eventId());
                auditLogService.logLoginEvent(event);
                acknowledgment.acknowledge();
                return;
            }

            // 3. Rate Limiting Check: protect against notification spamming
            if (!rateLimiterService.allowAlert(event.userId())) {
                log.warn("Rate limit exceeded for userId={}. Suppressing alert dispatch [eventId={}].",
                        event.userId(), event.eventId());
                auditLogService.logLoginEvent(event);
                acknowledgment.acknowledge();
                return;
            }

            // 4. Action: Audit Logging
            auditLogService.logLoginEvent(event);

            // 5. Action: Email Notification (triggered on new device/unrecognized environment)
            if (event.isNewDevice() && event.email() != null && !event.email().isBlank()) {
                log.info("New device login detected for userId={}. Triggering security email alert.", event.userId());
                emailNotificationService.sendNewLoginAlert(event);
            }

            // 6. Action: Push Notification (Firebase Cloud Messaging with tokenized link)
            if (event.userId() != null) {
                try {
                    List<String> activeTokens = deviceTokenRepository.findTokensByUserIds(List.of(event.userId().toString()));
                    if (activeTokens != null && !activeTokens.isEmpty()) {
                        log.info("Dispatching FCM push notification to {} active device(s) for userId={}",
                                activeTokens.size(), event.userId());

                        String verifyUrl = (trackingService != null)
                                ? trackingService.generateSignedVerificationUrl(event.eventId(), event.userId())
                                : "";

                        Map<String, String> data = Map.of(
                                "eventId", event.eventId() != null ? event.eventId().toString() : "",
                                "loginTime", event.loginTime() != null ? event.loginTime().toString() : "",
                                "verifyUrl", verifyUrl
                        );

                        firebasePushService.sendToMultipleDevices(
                                activeTokens,
                                "Security Alert: New Sign-in",
                                "New login from " + (event.device() != null ? event.device() : "a new device") + " verified via 2FA.",
                                data
                        );
                    }
                } catch (Exception pushEx) {
                    log.warn("Non-critical failure dispatching FCM push for userId={}: {}", event.userId(), pushEx.getMessage());
                }
            }

            // 7. Action: WhatsApp Notification (triggered on new device login)
            if (event.isNewDevice()) {
                try {
                    log.info("Triggering WhatsApp login alert for userId={}", event.userId());
                    whatsAppNotificationService.sendWhatsAppAlert(event);
                } catch (Exception waEx) {
                    log.warn("Non-critical failure dispatching WhatsApp alert for userId={}: {}", event.userId(), waEx.getMessage());
                }
            }

            // 8. Commit offset manually once processing succeeds
            acknowledgment.acknowledge();
            log.info("Successfully completed processing and committed offset for UserLoginEvent [eventId={}]", event.eventId());

        } catch (Exception ex) {
            log.error("Fatal error during UserLoginEvent processing [eventId={}, partition={}, offset={}]: {}",
                    event.eventId(), partition, offset, ex.getMessage(), ex);
            // Re-throw so Spring Kafka's DefaultErrorHandler catches it, runs retries, and forwards to DLT if exhausted
            throw ex;
        }
    }
}
