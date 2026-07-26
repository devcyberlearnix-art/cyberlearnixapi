package com.example.admin.controller;
import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.PaymentDto;
import com.example.admin.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ApiResponse<List<PaymentDto>> getAllPayments() {

        List<PaymentDto> payments = paymentService.getAllPayments();

        return new ApiResponse<>(
                true,
                "Payments fetched successfully",
                payments,
                LocalDateTime.now().toString()
        );
    }
    @GetMapping("/{id}")
    public ApiResponse<PaymentDto> getPaymentById(@PathVariable UUID id) {

        PaymentDto payment = paymentService.getPaymentById(id);

        if (payment == null) {
            return new ApiResponse<>(
                    false,
                    "Payment not found",
                    null,
                    LocalDateTime.now().toString()
            );
        }

        return new ApiResponse<>(
                true,
                "Payment fetched successfully",
                payment,
                LocalDateTime.now().toString()
        );
    }
}