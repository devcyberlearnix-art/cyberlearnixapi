package com.example.notification.controller;

import com.example.notification.dto.AnnouncementResponse;
import com.example.notification.dto.ApiResponse;
import com.example.notification.dto.CreateAnnouncementRequest;
import com.example.notification.dto.DetailedAnnouncementResponse;
import com.example.notification.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    public ResponseEntity<?> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request) {

        AnnouncementResponse response = announcementService.createAnnouncement(request);

        return ResponseEntity.ok().body(
                ApiResponse.success("Announcement created successfully", response)
        );
    }


    // =========================================
    // ✅ GET DETAILED ANNOUNCEMENTS (DYNAMIC)
    // =========================================
    @GetMapping("/course/{courseId}/detailed")
    public ResponseEntity<ApiResponse<List<DetailedAnnouncementResponse>>> getDetailedAnnouncements(
            @PathVariable Long courseId) {

        List<DetailedAnnouncementResponse> data =
                announcementService.getAnnouncementsDetailByCourseId(courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Announcements fetched successfully", data)
        );
    }
}