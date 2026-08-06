package com.example.instructorservice.controller;

import com.example.instructorservice.dto.DashboardResponseDTO;
import com.example.instructorservice.dto.InstructorEarningsResponse;
import com.example.instructorservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<DashboardResponseDTO> getInstructorDashboard(
            @PathVariable("id") UUID instructorId
    ) {
        DashboardResponseDTO response = dashboardService.getDashboard(instructorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/earnings")
    public ResponseEntity<InstructorEarningsResponse> getInstructorEarnings(
            @PathVariable("id") UUID instructorId
    ) {
        return ResponseEntity.ok(dashboardService.getInstructorEarnings(instructorId));
    }
}
