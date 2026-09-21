package com.lms.courseservice.controller;

import com.lms.courseservice.service.CloudinaryService;
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

    /**
     * Upload a banner image file to Cloudinary and return the secure URL.
     *
     * POST /api/v1/admin/banners/upload-image
     * Content-Type: multipart/form-data
     * Body: file (image file)
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Banner image uploaded successfully",
     *   "imageUrl": "https://res.cloudinary.com/dmvmvdefr/image/upload/...",
     *   "timestamp": "2026-09-19T..."
     * }
     */
    @PreAuthorize("hasAnyRole('MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping(value = "/admin/banners/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadBannerImage(
            @RequestPart("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success",   false,
                    "message",   "No file provided. Please attach an image file with the key 'file'.",
                    "timestamp", Instant.now().toString()
            ));
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success",   false,
                    "message",   "Invalid file type. Only image files are allowed.",
                    "timestamp", Instant.now().toString()
            ));
        }

        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success",   false,
                    "message",   "File size exceeds maximum limit of 5MB.",
                    "timestamp", Instant.now().toString()
            ));
        }

        String imageUrl = cloudinaryService.uploadImage(file);

        return ResponseEntity.ok(Map.of(
                "success",          true,
                "message",          "Banner image uploaded successfully",
                "imageUrl",         imageUrl,
                "originalFilename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "",
                "size",             file.getSize(),
                "timestamp",        Instant.now().toString()
        ));
    }
}
