package com.example.notification.service;


import com.example.notification.dto.PreferenceResponse;
import com.example.notification.dto.UpdatePreferenceRequest;
import com.example.notification.entity.UserPreference;
import com.example.notification.repository.UserPreferenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PreferenceService {

    private final UserPreferenceRepository repository;

    public PreferenceService(UserPreferenceRepository repository) {
        this.repository = repository;
    }

    public PreferenceResponse getPreferences() {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is missing or expired");
        }

        String userId = (String) auth.getPrincipal();
        UUID uuid = UUID.fromString(userId);

        UserPreference pref = repository.findByUserId(uuid)
                .orElseGet(() -> createDefaultPreference(uuid));

        return PreferenceResponse.builder()
                .emailEnabled(pref.isEmailEnabled())
                .pushEnabled(pref.isPushEnabled())
                .inAppEnabled(pref.isInAppEnabled())
                .build();
    }

    private UserPreference createDefaultPreference(UUID userId) {

        UserPreference pref = UserPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .pushEnabled(true)
                .inAppEnabled(true)
                .build();

        return repository.save(pref);
    }
    public Map<String, Object> updatePreferences(UpdatePreferenceRequest request) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is missing or expired");
        }

        String userId = (String) auth.getPrincipal();
        UUID userUuid = UUID.fromString(userId);

        UserPreference pref = repository.findByUserId(userUuid)
                .orElseGet(() -> createDefaultPreference(userUuid)); // ✅ FIXED

        if (request.getEmailEnabled() != null) {
            pref.setEmailEnabled(request.getEmailEnabled());
        }

        if (request.getPushEnabled() != null) {
            pref.setPushEnabled(request.getPushEnabled());
        }

        if (request.getInAppEnabled() != null) {
            pref.setInAppEnabled(request.getInAppEnabled());
        }

        pref.setUpdatedAt(LocalDateTime.now()); // ✅ now works

        repository.save(pref);

        return Map.of(
                "userId", userId,
                "preferences", Map.of(
                        "emailEnabled", pref.isEmailEnabled(),
                        "pushEnabled", pref.isPushEnabled(),
                        "inAppEnabled", pref.isInAppEnabled()
                ),
                "meta", Map.of(
                        "updatedAt", pref.getUpdatedAt().toString()
                )
        );

    }


}