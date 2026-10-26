package com.lms.wishlist_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for communicating with Cart Service.
 * Used for wishlist-to-cart integration.
 */
@FeignClient(name = "cart-service", url = "${cart-service.url:http://localhost:8081}")
public interface CartClient {

    /**
     * Add a course to the cart (service-to-service call).
     * This endpoint is used by wishlist-service when moving items from wishlist to cart.
     */
    @PostMapping("/api/v1/cart/internal/add")
    CartAddResponse addToCartInternal(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CartAddRequest request);

    /**
     * Request DTO for adding course to cart internally.
     */
    record CartAddRequest(Long courseId) {
    }

    /**
     * Response DTO for cart add operation.
     */
    record CartAddResponse(boolean success, String message, String courseId) {
    }
}
