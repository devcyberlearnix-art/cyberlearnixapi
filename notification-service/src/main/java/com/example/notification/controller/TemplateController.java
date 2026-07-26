package com.example.notification.controller;

import com.example.notification.dto.TemplateRequest;
import com.example.notification.dto.TemplateResponse;
import com.example.notification.dto.ApiResponse;
import com.example.notification.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService service;

    @PostMapping
    public ResponseEntity<?> createTemplate(
            @Valid @RequestBody TemplateRequest request
    ) {

        TemplateResponse response = service.createTemplate(request);

        return ResponseEntity.ok(
                ApiResponse.success("Template created successfully", response)
        );
    }
    @GetMapping
    public ResponseEntity<?> getTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Boolean active
    ) {

        Page<TemplateResponse> templates =
                service.getTemplates(page, size, channel, active);

        // ✅ Clean structured response
        Map<String, Object> responseData = Map.of(
                "templates", templates.getContent(),
                "currentPage", templates.getNumber(),
                "pageSize", templates.getSize(),
                "totalElements", templates.getTotalElements(),
                "totalPages", templates.getTotalPages(),
                "isLast", templates.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Templates fetched successfully", responseData)
        );
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody TemplateRequest request
    ) {

        TemplateResponse response = service.updateTemplate(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Template updated successfully", response)
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable String id) {

        TemplateResponse response = service.deleteTemplate(id);

        return ResponseEntity.ok(
                ApiResponse.success("Template deleted successfully", response)
        );
    }
}
