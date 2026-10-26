package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.LiveClassAttendanceResponse;
import com.lms.courseservice.dto.LiveClassAttendanceSummaryResponse;
import com.lms.courseservice.service.LiveClassAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/live-classes")
@RequiredArgsConstructor
public class LiveClassAttendanceController {

    private final LiveClassAttendanceService attendanceService;

    private UUID extractUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    private String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    // ==================== STUDENT APIs ====================

    /**
     * Join a live class and create attendance record
     */
    @PreAuthorize("hasAnyRole('STUDENT','USER','INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping("/{liveClassId}/attendance/join")
    public ResponseEntity<ApiResponse<LiveClassAttendanceResponse>> joinLiveClass(@PathVariable Long liveClassId) {
        UUID studentId = extractUserId();
        LiveClassAttendanceResponse response = attendanceService.joinLiveClass(liveClassId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<LiveClassAttendanceResponse>builder()
                        .success(true)
                        .message("Successfully joined live class")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Leave a live class and record leave time
     */
    @PreAuthorize("hasAnyRole('STUDENT','USER','INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{liveClassId}/attendance/leave")
    public ResponseEntity<ApiResponse<LiveClassAttendanceResponse>> leaveLiveClass(@PathVariable Long liveClassId) {
        UUID studentId = extractUserId();
        LiveClassAttendanceResponse response = attendanceService.leaveLiveClass(liveClassId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<LiveClassAttendanceResponse>builder()
                        .success(true)
                        .message("Successfully left live class")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get student's own attendance for a live class
     */
    @PreAuthorize("hasAnyRole('STUDENT','USER','INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/{liveClassId}/attendance/me")
    public ResponseEntity<ApiResponse<LiveClassAttendanceResponse>> getMyAttendance(@PathVariable Long liveClassId) {
        UUID studentId = extractUserId();
        LiveClassAttendanceResponse response = attendanceService.getMyAttendance(liveClassId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<LiveClassAttendanceResponse>builder()
                        .success(true)
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== INSTRUCTOR/ADMIN APIs ====================

    /**
     * Get all attendance records for a live class
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/{liveClassId}/attendance")
    public ResponseEntity<ApiResponse<List<LiveClassAttendanceResponse>>> getAttendanceByLiveClass(@PathVariable Long liveClassId) {
        UUID instructorId = extractUserId();
        List<LiveClassAttendanceResponse> response = attendanceService.getAttendanceByLiveClass(liveClassId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<List<LiveClassAttendanceResponse>>builder()
                        .success(true)
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get attendance summary for a live class
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @GetMapping("/{liveClassId}/attendance/summary")
    public ResponseEntity<ApiResponse<LiveClassAttendanceSummaryResponse>> getAttendanceSummary(@PathVariable Long liveClassId) {
        UUID instructorId = extractUserId();
        LiveClassAttendanceSummaryResponse response = attendanceService.getAttendanceSummary(liveClassId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<LiveClassAttendanceSummaryResponse>builder()
                        .success(true)
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }
}
