package com.lms.orderservice.client;

import com.lms.orderservice.client.dto.cart.ApiResponse;
import com.lms.orderservice.client.dto.cart.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cart-service", url = "${cart.service.url}")
public interface CartClient {

    @GetMapping("/api/cart/{userId}/view")
    ApiResponse<CartResponse> getCart(@PathVariable("userId") String userId);

    @DeleteMapping("/api/cart/{userId}/clear-all")
    ApiResponse<CartResponse> clearCart(@PathVariable("userId") String userId);
}
