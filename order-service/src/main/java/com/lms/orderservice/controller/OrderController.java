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

        String userId = (request != null && request.getUserId() != null && !request.getUserId().isBlank())
                ? request.getUserId()
                : (authentication != null ? authentication.getName() : null);

        return ResponseEntity.ok(ApiResponse.success(
            "Order created successfully", orderService.createOrder(userId, request)));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(Authentication authentication, @PathVariable String orderId) {
        var order = isAdmin(authentication)
                ? orderService.getOrder(orderId)
                : orderService.getOrderForUser(orderId, authentication != null ? authentication.getName() : null);
        return ResponseEntity.ok(ApiResponse.success(
            "Order retrieved successfully", order));
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
            "Orders retrieved successfully", orderService.getOrdersByUser(authentication != null ? authentication.getName() : null)));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAllOrdersForAdmin() {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved successfully", orderService.getAllOrders()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getOrderAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(
                "Order analytics retrieved successfully", orderService.getOrderAnalytics()));
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
        String message = isAdmin(authentication)
                ? orderService.cancelOrderAsAdmin(orderId)
                : orderService.cancelOrder(orderId, authentication != null ? authentication.getName() : null);
        return ResponseEntity.ok(ApiResponse.success(
            "Order cancelled successfully", message));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> refund(Authentication authentication, @PathVariable String orderId) {
        var order = isAdmin(authentication)
                ? orderService.refundOrderAsAdmin(orderId)
                : orderService.refundOrder(orderId, authentication != null ? authentication.getName() : null);
        return ResponseEntity.ok(ApiResponse.success(
            "Refund processed successfully", order));
    }
}