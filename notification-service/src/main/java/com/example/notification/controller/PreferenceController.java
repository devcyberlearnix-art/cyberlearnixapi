package com.example.notification.controller;

import com.example.notification.dto.PreferenceResponse;
import com.example.notification.dto.UpdatePreferenceRequest;
import com.example.notification.service.PreferenceService;
import com.example.notification.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PreferenceResponse>> getPreferences() {

        PreferenceResponse response = preferenceService.getPreferences();

        return ResponseEntity.ok(
                ApiResponse.success("Preferences fetched successfully", response)
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePreferences(
            @RequestBody UpdatePreferenceRequest request) {
        if (request == null || (request.getEmailEnabled() == null && request.getPushEnabled() == null && request.getInAppEnabled() == null)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Preference update payload cannot be empty");
        }

        Map<String, Object> response =
                preferenceService.updatePreferences(request);

        return ResponseEntity.ok(
                ApiResponse.success("Preferences updated successfully", response)
        );

    }

}
