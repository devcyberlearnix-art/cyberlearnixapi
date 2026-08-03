package com.example.instructorservice.controller;


import com.example.instructorservice.dto.ModuleRequest;
import com.example.instructorservice.dto.ModuleResponse;
import com.example.instructorservice.dto.ResourceResponse;
import com.example.instructorservice.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructors/{instructorId}/courses/{courseId}/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    /**
     * Add a new module to a course for a specific instructor
     * @param instructorId UUID of the instructor
     * @param courseId UUID of the course
     * @param request ModuleRequest containing title and description
     * @return ResponseEntity with ModuleResponse (detailed)
     */
    @PostMapping
    public ResponseEntity<ModuleResponse> addModule(
            @PathVariable("instructorId") UUID instructorId,
            @PathVariable("courseId") UUID courseId,
            @RequestBody ModuleRequest request
    ) {
        ModuleResponse response = moduleService.addModule(instructorId, courseId, request);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{moduleId}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable UUID instructorId,
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @RequestBody ModuleRequest request
    ) {
        ModuleResponse response = moduleService.updateModule(instructorId, courseId, moduleId, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{moduleId}")
    public ResponseEntity<ModuleResponse> deleteModule(
            @PathVariable UUID instructorId,
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        ModuleResponse response = moduleService.deleteModule(instructorId, courseId, moduleId);
        return ResponseEntity.ok(response);
    }
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResourceResponse> uploadResource(
            @PathVariable UUID instructorId,
            @PathVariable UUID courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type
    ) {
        return ResponseEntity.ok(
                moduleService.uploadResource(instructorId, courseId, file, type)
        );
    }
}
