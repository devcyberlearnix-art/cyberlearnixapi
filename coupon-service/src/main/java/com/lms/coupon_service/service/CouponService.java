package com.lms.coupon_service.service;

import com.lms.coupon_service.dto.*;
import com.lms.coupon_service.entity.Coupon;
import java.util.List;
import java.util.Map;

public interface CouponService {
    Map<String, Object> createCoupon(Coupon coupon);

    Map<String, Object> createCouponFromMap(Map<String, Object> couponData);

    CouponDetailsResponse createCoupon(CouponCreateRequest request, String createdById, String createdByRole);

    List<CouponDetailsResponse> getAllCoupons();

    CouponDetailsResponse getCouponDetails(String couponId);

    CouponDetailsResponse updateCoupon(String couponId, CouponCreateRequest request, String updatedById,
            String updatedByRole);

    CouponDetailsResponse activateCoupon(String couponId);

    CouponDetailsResponse deactivateCoupon(String couponId);

    List<CouponDetailsResponse> getMyCoupons(String userId);

    List<String> getCampaignNames();

    Coupon findByCode(String code);

    void deleteById(String id);

    ValidationResponse validateCoupon(ValidateRequest request, String userId);

    Map<String, Object> redeemCoupon(RedeemRequest request, String userId);

    ValidationResponse autoApply(AutoApplyRequest request, String userId);

    BulkResponse bulkGenerate(BulkRequest request);

    List<Coupon> getCouponsByCampaignId(String campaignId);

    List<Coupon> getCouponsByUserId(String userId);

    Map<String, Object> generateReferralCoupon(String referrerId);

    Map<String, Object> assignCouponToUser(String code, String userId);
}