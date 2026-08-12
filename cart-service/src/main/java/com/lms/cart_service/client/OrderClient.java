package com.lms.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.Instant;
import java.util.List;

@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:8084}")
public interface OrderClient {

    @PostMapping("/api/v1/orders/create")
    OrderApiResponse createOrder(
            @RequestHeader("Authorization") String authorization,
            @RequestBody OrderCreateRequest request);

    record OrderCreateRequest(List<Long> courseIds, String couponCode) {
    }

    record OrderApiResponse(boolean success, String message, OrderData data, Instant timestamp) {
    }

    record OrderData(String orderId, String userId, Double totalAmount, String status) {
    }
}