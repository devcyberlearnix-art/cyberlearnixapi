package com.example.admin.controller;

import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.OrderDto;
import com.example.admin.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<List<OrderDto>> getAllOrders(
            @RequestHeader("Authorization") String authorization) {
        List<OrderDto> orders = orderService.getAllOrders(authorization);
        return new ApiResponse<>(
                true,
                "Orders fetched successfully",
                orders  ,
                LocalDateTime.now().toString()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDto> getOrderById(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {
        OrderDto order = orderService.getOrderById(id, authorization);
        if (order == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Order not found");
        }
        return new ApiResponse<>(true, "Order fetched successfully", order, LocalDateTime.now().toString());
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderDto> updateOrderStatus(
            @PathVariable String id,
            @RequestParam String status,
            @RequestHeader("Authorization") String authorization) {
        OrderDto updatedOrder = orderService.updateOrderStatus(id, status, authorization);
        if (updatedOrder == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Order not found or update failed");
        }
        return new ApiResponse<>(true, "Order status updated successfully", updatedOrder, LocalDateTime.now().toString());
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<OrderDto> processRefund(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {
        try {
            OrderDto refundedOrder = orderService.processRefund(id, authorization);
            if (refundedOrder == null) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Order not found or refund failed");
            }
            return new ApiResponse<>(true, "Refund processed successfully", refundedOrder, LocalDateTime.now().toString());
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            org.springframework.http.HttpStatus status = (e.getMessage() != null && e.getMessage().contains("404"))
                    ? org.springframework.http.HttpStatus.NOT_FOUND
                    : org.springframework.http.HttpStatus.BAD_REQUEST;
            throw new org.springframework.web.server.ResponseStatusException(status, "Refund failed: " + e.getMessage());
        }
    }
}
