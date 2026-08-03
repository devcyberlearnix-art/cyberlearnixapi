package com.example.admin.service;

import com.example.admin.client.AdminCouponServiceClient;
import com.example.admin.client.AdminCouponServiceClient.CouponDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final AdminCouponServiceClient couponServiceClient;

    public List<CouponDTO> getAllCoupons() {
        return couponServiceClient.getAllCoupons();
    }

    public CouponDTO getCouponById(String couponId) {
        return couponServiceClient.getCouponById(couponId);
    }

    public List<String> getCampaigns() {
        return couponServiceClient.getCampaigns();
    }
}
