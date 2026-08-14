package com.lms.orderservice.controller;

import com.lms.orderservice.dto.CreateOrderRequest;
import com.lms.orderservice.dto.ApiResponse;
import com.lms.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(Authentication authentication,
                                         @RequestBody CreateOrderRequest request) {

        String userId = authentication.getName();

        return ResponseEntity.ok(ApiResponse.success(
            "Order created successfully", orderService.createOrder(userId, request)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
            "Order retrieved successfully", orderService.getOrderForUser(orderId, authentication.getName())));
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
            "Orders retrieved successfully", orderService.getOrdersByUser(authentication.getName())));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAllOrdersForAdmin() {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved successfully", orderService.getAllOrders()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(
            "Orders retrieved successfully", orderService.getOrdersByUser(userId)));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String orderId,
                                          @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(
            "Order status updated successfully", orderService.updateStatus(orderId, status)));
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
            "Order cancelled successfully", orderService.cancelOrder(orderId, authentication.getName())));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> refund(Authentication authentication, @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
            "Refund processed successfully", orderService.refundOrder(orderId, authentication.getName())));
    }
}