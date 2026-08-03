package com.lms.orderservice.client;

import com.lms.orderservice.client.dto.cart.ApiResponse;
import com.lms.orderservice.client.dto.cart.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cart-service", url = "${cart.service.url}")
public interface CartClient {

    @GetMapping("/api/v1/cart/internal/{userId}")
    ApiResponse<CartResponse> getCart(@PathVariable String userId);

    @DeleteMapping("/api/v1/cart/internal/{userId}")
    ApiResponse<Void> clearCart(@PathVariable String userId);
}
