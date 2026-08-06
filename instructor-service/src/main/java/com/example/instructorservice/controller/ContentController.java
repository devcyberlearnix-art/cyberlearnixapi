package com.example.instructorservice.controller;

import com.example.instructorservice.dto.ContentResponse;
import com.example.instructorservice.dto.PublishRequest;
import com.example.instructorservice.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructors/{instructorId}/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PatchMapping("/{contentId}/publish")
    public ResponseEntity<ContentResponse> publishContent(
            @PathVariable UUID instructorId,
            @PathVariable UUID contentId,
            @RequestBody PublishRequest request
    ) {
        ContentResponse response = contentService.publishContent(
                instructorId,
                contentId,
                request.isPublish()
        );

        return ResponseEntity.ok(response);
    }
}
