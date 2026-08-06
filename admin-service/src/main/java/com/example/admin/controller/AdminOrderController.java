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
    public ApiResponse<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return new ApiResponse<>(
                true,
                "Orders fetched successfully",
                orders  ,
                LocalDateTime.now().toString()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDto> getOrderById(@PathVariable String id) {
        OrderDto order = orderService.getOrderById(id);
        if (order == null) {
            return new ApiResponse<>(false, "Order not found", null, LocalDateTime.now().toString());
        }
        return new ApiResponse<>(true, "Order fetched successfully", order, LocalDateTime.now().toString());
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderDto> updateOrderStatus(
            @PathVariable String id,
            @RequestParam String status) {
        OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
        if (updatedOrder == null) {
            return new ApiResponse<>(false, "Order not found or update failed", null, LocalDateTime.now().toString());
        }
        return new ApiResponse<>(true, "Order status updated successfully", updatedOrder, LocalDateTime.now().toString());
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<OrderDto> processRefund(@PathVariable String id) {
        try {
            OrderDto refundedOrder = orderService.processRefund(id);
            if (refundedOrder == null) {
                return new ApiResponse<>(false, "Refund failed or order not found", null, LocalDateTime.now().toString());
            }
            return new ApiResponse<>(true, "Refund processed successfully", refundedOrder, LocalDateTime.now().toString());
        } catch (RuntimeException e) {
            return new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now().toString());
        }
    }
}
