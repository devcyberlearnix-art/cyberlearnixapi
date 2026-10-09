package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.InstructorProfileDTO;
import com.user.register.service.InstructorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
public class InstructorProfileController {

    private final InstructorProfileService instructorProfileService;

    @GetMapping("/{instructorId}/profile")
    public ResponseEntity<ApiResponse<InstructorProfileDTO>> getInstructorProfile(
            @PathVariable UUID instructorId) {
        try {
            InstructorProfileDTO profile = instructorProfileService.getInstructorProfile(instructorId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Instructor profile fetched successfully", profile, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }
}
