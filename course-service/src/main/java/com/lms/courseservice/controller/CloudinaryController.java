package com.lms.courseservice.controller;

import com.lms.courseservice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/upload", "/api/v1/courses/upload"})
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    /**
     * Upload a video file to Cloudinary and return the secure URL.
     *
     * POST /api/v1/upload/video
     * Content-Type: multipart/form-data
     * Body: file (video file)
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Video uploaded successfully",
     *   "url": "https://res.cloudinary.com/dmvmvdefr/video/upload/...",
     *   "timestamp": "2026-08-14T..."
     * }
     */
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

        String url = cloudinaryService.uploadVideo(file);

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
