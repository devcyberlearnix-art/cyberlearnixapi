package com.lms.orderservice.client;

import com.lms.orderservice.client.dto.coupon.RedeemRequest;
import com.lms.orderservice.client.dto.coupon.ValidateRequest;
import com.lms.orderservice.client.dto.coupon.ValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "coupon-service", url = "${coupon.service.url}")
public interface CouponClient {

    @PostMapping("/api/v1/coupons/validate")
    ValidationResponse validate(@RequestBody ValidateRequest request);

    @PostMapping("/api/v1/coupons/redeem")
    Map<String, Object> redeem(@RequestBody RedeemRequest request);
}
