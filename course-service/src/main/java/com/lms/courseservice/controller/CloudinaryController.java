package com.lms.courseservice.controller;

import com.lms.courseservice.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/upload", "/api/v1/courses/upload"})
@RequiredArgsConstructor
public class CloudinaryController {

    private final GoogleDriveService googleDriveService;

    /**
     * Upload a video file to Google Drive and return the public URL.
     *
     * POST /api/v1/upload/video
     * Content-Type: multipart/form-data
     * Body: file (video file)
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Video uploaded successfully",
     *   "url": "https://drive.google.com/uc?id=...",
     *   "timestamp": "2026-08-14T..."
     * }
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestPart("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success",   false,
                    "message",   "No file provided. Please attach a video file with the key 'file'.",
                    "timestamp", Instant.now().toString()
            ));
        }

        String url = googleDriveService.uploadVideo(file);

        return ResponseEntity.ok(Map.of(
                "success",          true,
                "message",          "Video uploaded successfully",
                "url",              url,
                "originalFilename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "",
                "size",             file.getSize(),
                "timestamp",        Instant.now().toString()
        ));
    }
}
