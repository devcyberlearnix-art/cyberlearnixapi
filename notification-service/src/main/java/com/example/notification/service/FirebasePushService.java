package com.example.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FirebasePushService {

    private static final Logger log = LoggerFactory.getLogger(FirebasePushService.class);

    private final FirebaseMessaging firebaseMessaging;

    /**
     * Send a push notification to a single device token.
     *
     * @return the Firebase message ID
     */
    public String sendToDevice(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = firebaseMessaging.send(messageBuilder.build());
            log.info("✅ Push sent to device [token={}...]: messageId={}", fcmToken.substring(0, Math.min(10, fcmToken.length())), response);
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("❌ Failed to send push to device [token={}...]: {}", fcmToken.substring(0, Math.min(10, fcmToken.length())), e.getMessage());
            throw new RuntimeException("Failed to send push notification: " + e.getMessage(), e);
        }
    }

    /**
     * Send a push notification to multiple device tokens using multicast.
     *
     * @return BatchResponse with success/failure counts
     */
    public BatchResponse sendToMultipleDevices(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(fcmTokens)
                    .setNotification(notification);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            BatchResponse response = firebaseMessaging.sendEachForMulticast(messageBuilder.build());

            log.info("✅ Multicast push sent: success={}, failure={}, total={}",
                    response.getSuccessCount(), response.getFailureCount(), fcmTokens.size());

            // Log individual failures for debugging
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.warn("   ⚠️ Token [{}] failed: {}", i,
                                responses.get(i).getException() != null
                                        ? responses.get(i).getException().getMessage()
                                        : "unknown error");
                    }
                }
            }

            return response;

        } catch (FirebaseMessagingException e) {
            log.error("❌ Failed to send multicast push: {}", e.getMessage());
            throw new RuntimeException("Failed to send multicast push notification: " + e.getMessage(), e);
        }
    }

    /**
     * Send a push notification to a Firebase topic.
     *
     * @return the Firebase message ID
     */
    public String sendToTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic)
                    .setNotification(notification);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = firebaseMessaging.send(messageBuilder.build());
            log.info("✅ Push sent to topic [{}]: messageId={}", topic, response);
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("❌ Failed to send push to topic [{}]: {}", topic, e.getMessage());
            throw new RuntimeException("Failed to send topic push notification: " + e.getMessage(), e);
        }
    }
}
