package com.lms.cart_service.service;

import com.lms.cart_service.dto.AddCourseResponse;
import com.lms.cart_service.dto.CartRequest;
import com.lms.cart_service.dto.CartResponse;
import com.lms.cart_service.dto.CartSummaryResponse;
import com.lms.cart_service.dto.CheckoutResponse;
import com.lms.cart_service.dto.CouponApplyResponse;
import com.lms.cart_service.dto.MyCartResponse;
import com.lms.cart_service.dto.RemoveCourseResponse;

public interface CartService {
    AddCourseResponse addToCart(String userId, CartRequest request);

    MyCartResponse getUserCart(String userId);

    CartSummaryResponse getCartSummary(String userId);

    CouponApplyResponse applyCouponToCart(String userId, String couponCode);

    CouponApplyResponse removeCouponFromCart(String userId);

    RemoveCourseResponse removeCourseFromCart(String userId, Long courseId);

    void clearFullCart(String userId);

    CheckoutResponse checkoutCart(String userId);
}