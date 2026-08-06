package com.example.notification.controller;

import com.example.notification.dto.*;
import com.example.notification.service.AssignmentReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentReminderController {
    private final AssignmentReminderService service;
    @PostMapping("/reminder")
    public ResponseEntity<ApiResponse<AssignmentReminderResponse>> sendReminder(
            @RequestBody AssignmentReminderRequest request) {

        AssignmentReminderResponse response = service.sendReminder(request);

        return ResponseEntity.ok(
                ApiResponse.success("Assignment reminder processed successfully", response)
        );
    }
}