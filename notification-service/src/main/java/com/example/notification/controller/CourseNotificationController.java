package com.example.notification.controller;

import com.example.notification.dto.ApiResponse;
import com.example.notification.dto.CourseNotificationRequest;
import com.example.notification.dto.CourseNotificationResponse;
import com.example.notification.service.CourseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class CourseNotificationController {

    private final CourseNotificationService service;

    @PostMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<CourseNotificationResponse>> notifyCourse(
            @PathVariable Long courseId,
            @RequestBody CourseNotificationRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Course notification sent successfully",
                        service.notifyCourse(courseId, request)
                )
        );
    }
}