package com.example.instructorservice.controller;

import com.example.instructorservice.dto.AnnouncementRequest;
import com.example.instructorservice.dto.AnnouncementResponse;
import com.example.instructorservice.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping("/{id}/courses/{courseId}/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @PathVariable("id") UUID instructorId,
            @PathVariable UUID courseId,
            @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.ok(
                announcementService.createAnnouncement(instructorId, courseId, request)
        );
    }
}