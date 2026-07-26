package com.example.instructorservice.controller;


import com.example.instructorservice.dto.CourseMessageRequest;
import com.example.instructorservice.dto.CourseMessageResponse;
import com.example.instructorservice.service.CourseMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
public class CourseMessageController {

    private final CourseMessageService courseMessageService;

    @PostMapping("/{id}/courses/{courseId}/messages")
    public ResponseEntity<CourseMessageResponse> sendMessage(
            @PathVariable("id") UUID instructorId,
            @PathVariable UUID courseId,
            @RequestBody CourseMessageRequest request
    ) {
        return ResponseEntity.ok(
                courseMessageService.sendMessage(instructorId, courseId, request)
        );
    }
}