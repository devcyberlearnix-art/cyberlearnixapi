package com.example.notification.service;

import com.example.notification.dto.BroadcastRequest;
import com.example.notification.dto.DlqReprocessResponse;
import com.example.notification.dto.NotificationSettingsRequest;
import com.example.notification.dto.SystemHealthResponse;
import com.example.notification.entity.Template;
import com.example.notification.entity.UserPreference;
import com.example.notification.repository.DeviceTokenRepository;
import com.example.notification.repository.TemplateRepository;
import com.example.notification.repository.NotificationRepository;

import com.example.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final TemplateRepository templateRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository; // ✅ FIXED
    private final UserPreferenceRepository userPreferenceRepository;
    // =========================
    // 🔥 BROADCAST
    // =========================
    public String sendBroadcast(BroadcastRequest request) {

        String finalMessage = request.getMessage();

        // ✅ Apply template if exists
        if (request.getTemplateName() != null) {

            Template template = templateRepository.findByName(request.getTemplateName())
                    .orElseThrow(() -> new RuntimeException("Template not found"));

            finalMessage = applyVariables(template.getContent(), request.getVariables());
        }

        // ✅ Fetch tokens
        List<String> tokens;

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            tokens = deviceTokenRepository.findTokensByUserIds(request.getUserIds());
        } else {
            tokens = deviceTokenRepository.findAllActiveTokens();
        }

        // 🔥 Mock send (replace with FCM later)
        for (String token : tokens) {
            System.out.println("Sending to: " + token + " → " + finalMessage);
        }

        return "Broadcast sent to " + tokens.size() + " users";
    }

    // =========================
    // 🔁 TEMPLATE VARIABLES
    // =========================
    private String applyVariables(String content, Map<String, String> variables) {

        if (variables == null) return content;

        String result = content;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return result;
    }

    // =========================
    // 🔁 DLQ (CURRENTLY MOCK)
    // =========================
    public DlqReprocessResponse reprocessDlq() {

        List<String> dlqMessages = fetchMessagesFromDlq();

        int successCount = 0;
        int failureCount = 0;

        List<DlqReprocessResponse.FailedMessage> failedMessages = new ArrayList<>();

        for (String message : dlqMessages) {
            try {
                processMessage(message);
                successCount++;

            } catch (Exception ex) {
                failureCount++;

                failedMessages.add(
                        DlqReprocessResponse.FailedMessage.builder()
                                .notificationId(extractNotificationId(message))
                                .error(ex.getMessage())
                                .build()
                );
            }
        }

        return DlqReprocessResponse.builder()
                .timestamp(Instant.now())
                .status(200)
                .message("DLQ reprocessing completed")
                .data(
                        DlqReprocessResponse.DataPayload.builder()
                                .totalMessages(dlqMessages.size())
                                .successCount(successCount)
                                .failureCount(failureCount)
                                .failedMessages(failedMessages)
                                .build()
                )
                .build();
    }

    private List<String> fetchMessagesFromDlq() {
        return Arrays.asList("msg-1", "msg-2", "msg-3");
    }

    private void processMessage(String message) {
        if ("msg-2".equals(message)) {
            throw new RuntimeException("Failed to process message");
        }
    }

    private String extractNotificationId(String message) {
        return UUID.randomUUID().toString();
    }

    // =========================
    // ❤️ SYSTEM HEALTH (FIXED)
    // =========================
    public SystemHealthResponse getSystemHealth() {

        Map<String, String> checks = new HashMap<>();

        // ✅ DATABASE CHECK
        try {
            notificationRepository.count(); // ✅ FIXED (no static call)
            checks.put("database", "UP");
        } catch (Exception e) {
            checks.put("database", "DOWN");
        }

        // ✅ NOTIFICATION TABLE CHECK
        try {
            long count = notificationRepository.count(); // ✅ FIXED
            checks.put("notifications", "UP (" + count + " records)");
        } catch (Exception e) {
            checks.put("notifications", "DOWN");
        }

        // ✅ DISK CHECK
        File disk = new File("/");
        long freeSpace = disk.getFreeSpace() / (1024 * 1024);
        long totalSpace = disk.getTotalSpace() / (1024 * 1024);

        String diskStatus = freeSpace > 100 ? "UP" : "LOW";
        checks.put("diskSpace", diskStatus + " (" + freeSpace + "MB free / " + totalSpace + "MB)");

        // ✅ MEMORY CHECK
        long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        checks.put("memory", "UP (" + freeMem + "MB free / " + totalMem + "MB)");

        // ✅ UPTIME
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration duration = Duration.ofMillis(uptimeMillis);

        String uptime = duration.toHours() + "h "
                + (duration.toMinutes() % 60) + "m "
                + (duration.getSeconds() % 60) + "s";

        // ✅ OVERALL STATUS
        String overallStatus = checks.values().stream()
                .anyMatch(v -> v.startsWith("DOWN")) ? "DOWN" : "UP";

        return SystemHealthResponse.builder()
                .timestamp(Instant.now())
                .status(overallStatus)
                .service("notification-service")
                .version("1.0.0")
                .uptime(uptime)
                .checks(checks)
                .build();
    }
    public Map<String, Object> updateNotificationSettings(NotificationSettingsRequest request) {

        List<UserPreference> preferences = userPreferenceRepository.findAll(); // ✅ correct

        int totalUsers = preferences.size();
        int updatedUsers = 0;

        for (UserPreference pref : preferences) {

            boolean changed = false;

            // ⚠️ MAKE SURE THESE FIELDS EXIST IN ENTITY
            if (request.getEmailEnabled() != null) {
                pref.setEmailEnabled(request.getEmailEnabled());
                changed = true;
            }

            if (request.getPushEnabled() != null) {
                pref.setPushEnabled(request.getPushEnabled());
                changed = true;
            }

            if (request.getSmsEnabled() != null) {
                pref.setSmsEnabled(request.getSmsEnabled());
                changed = true;
            }

            if (changed) {
                updatedUsers++;
            }
        }

        userPreferenceRepository.saveAll(preferences); // ✅ correct

        return Map.of(
                "totalUsers", totalUsers,
                "updatedUsers", updatedUsers,
                "unchangedUsers", totalUsers - updatedUsers,
                "emailEnabled", request.getEmailEnabled(),
                "pushEnabled", request.getPushEnabled(),
                "smsEnabled", request.getSmsEnabled()
        );
    }
}