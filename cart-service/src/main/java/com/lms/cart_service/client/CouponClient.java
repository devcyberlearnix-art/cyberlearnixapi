package com.lms.cart_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "coupon-service", url = "${coupon-service.url:http://localhost:8082}")
public interface CouponClient {

    @GetMapping("/api/v1/coupons/validate/{code}")
    Double getDiscount(@PathVariable("code") String code);
}