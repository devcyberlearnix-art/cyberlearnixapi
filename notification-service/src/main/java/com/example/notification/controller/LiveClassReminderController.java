package com.example.notification.controller;

import com.example.notification.dto.*;
import com.example.notification.service.LiveClassReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/live-class")
@RequiredArgsConstructor
public class LiveClassReminderController {

    private final LiveClassReminderService reminderService;

    @PostMapping("/reminder")
    public ResponseEntity<ApiResponse<LiveClassReminderResponse>> sendReminder(
            @RequestBody LiveClassReminderRequest request) {

        LiveClassReminderResponse response = reminderService.sendReminder(request);

        return ResponseEntity.ok(
                ApiResponse.success("Live class reminder sent successfully", response)
        );
    }
}