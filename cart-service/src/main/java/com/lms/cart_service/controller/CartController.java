package com.lms.cart_service.controller;

import com.lms.cart_service.dto.ApiResponse;
import com.lms.cart_service.dto.AddCourseResponse;
import com.lms.cart_service.dto.CartRequest;
import com.lms.cart_service.dto.CartResponse;
import com.lms.cart_service.dto.CartSummaryResponse;
import com.lms.cart_service.dto.CheckoutResponse;
import com.lms.cart_service.dto.CouponApplyResponse;
import com.lms.cart_service.dto.CouponRequest;
import com.lms.cart_service.dto.MyCartResponse;
import com.lms.cart_service.dto.RemoveCourseResponse;
import com.lms.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddCourseResponse>> addToCart(Authentication auth,
            @RequestBody CartRequest request) {
        String userId = auth.getName();
        AddCourseResponse data = cartService.addToCart(userId, request);
        return ResponseEntity.status(201).body(ApiResponse.success("Course added to cart successfully.", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MyCartResponse>> getCart(Authentication auth) {
        String userId = auth.getName();
        MyCartResponse data = cartService.getUserCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully.", data));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCartSummary(Authentication auth) {
        String userId = auth.getName();
        CartSummaryResponse data = cartService.getCartSummary(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart summary retrieved successfully.", data));
    }

    @PutMapping("/coupon")
    public ResponseEntity<ApiResponse<CouponApplyResponse>> applyCoupon(Authentication auth,
            @RequestBody CouponRequest request) {
        String userId = auth.getName();
        CouponApplyResponse data = cartService.applyCouponToCart(userId, request.getCouponCode());
        return ResponseEntity.ok(ApiResponse.success("Coupon applied successfully.", data));
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<ApiResponse<CouponApplyResponse>> removeCoupon(Authentication auth) {
        String userId = auth.getName();
        CouponApplyResponse data = cartService.removeCouponFromCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Coupon removed successfully.", data));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<RemoveCourseResponse>> removeCourse(Authentication auth,
            @PathVariable Long courseId) {
        String userId = auth.getName();
        RemoveCourseResponse data = cartService.removeCourseFromCart(userId, courseId);
        return ResponseEntity.ok(ApiResponse.success("Course removed from cart successfully.", data));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication auth) {
        String userId = auth.getName();
        cartService.clearFullCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully."));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(Authentication auth) {
        String userId = auth.getName();
        CheckoutResponse data = cartService.checkoutCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Checkout initiated successfully.", data));
    }
}