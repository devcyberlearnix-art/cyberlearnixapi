package com.example.notification.controller;

import com.example.notification.dto.*;
import com.example.notification.service.CertificateNotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateNotifyController {

    private final CertificateNotifyService service;

    @PostMapping("/notify")
    public ResponseEntity<ApiResponse<CertificateNotifyResponse>> notifyUsers(
            @RequestBody CertificateNotifyRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Certificate notification processed successfully",
                        service.notifyUsers(request)
                )
        );
    }
}
