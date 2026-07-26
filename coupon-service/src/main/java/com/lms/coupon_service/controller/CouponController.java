package com.lms.coupon_service.controller;

import com.lms.coupon_service.dto.*;
import com.lms.coupon_service.entity.Coupon;
import com.lms.coupon_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> create(@RequestBody CouponCreateRequest req) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdById = authentication != null ? authentication.getName() : null;
        String createdByRole = authentication != null ? authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse("UNKNOWN") : "UNKNOWN";

        CouponDetailsResponse details = service.createCoupon(req, createdById, createdByRole);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully.", details));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkResponse>> bulk(@RequestBody BulkRequest request) {
        if (request.getCreatorRole() == null || request.getCreatorRole().isBlank()) {
            request.setCreatorRole("ADMIN");
        }
        BulkResponse resp = service.bulkGenerate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk coupons generated successfully.", resp));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponDetailsResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("All coupons", service.getAllCoupons()));
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> getDetails(@PathVariable String couponId) {
        return ResponseEntity.ok(ApiResponse.success("Coupon details", service.getCouponDetails(couponId)));
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<Double> validateByCode(@PathVariable String code) {
        try {
            Coupon coupon = service.findByCode(code);
            return ResponseEntity.ok(coupon != null && coupon.isActive() ? coupon.getDiscountValue() : 0.0);
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(0.0);
        }
    }

    @PutMapping("/{couponId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> update(@PathVariable String couponId,
            @RequestBody CouponCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String updatedById = authentication != null ? authentication.getName() : null;
        String updatedByRole = authentication != null ? authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse("UNKNOWN") : "UNKNOWN";

        return ResponseEntity.ok(ApiResponse.success("Coupon updated successfully.",
                service.updateCoupon(couponId, request, updatedById, updatedByRole)));
    }

    @DeleteMapping("/{couponId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable String couponId) {
        service.deleteById(couponId);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted successfully", Map.of("couponId", couponId)));
    }

    @PatchMapping("/{couponId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> activate(@PathVariable String couponId) {
        return ResponseEntity
                .ok(ApiResponse.success("Coupon activated successfully.", service.activateCoupon(couponId)));
    }

    @PatchMapping("/{couponId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> deactivate(@PathVariable String couponId) {
        return ResponseEntity
                .ok(ApiResponse.success("Coupon deactivated successfully.", service.deactivateCoupon(couponId)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CouponDetailsResponse>>> getMyCoupons() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(ApiResponse.success("My coupons", service.getMyCoupons(userId)));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<List<String>>> getCampaigns() {
        return ResponseEntity.ok(ApiResponse.success("Campaigns retrieved successfully.", service.getCampaignNames()));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ValidationResponse>> validate(@RequestBody ValidateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Validation result", service.validateCoupon(request)));
    }

    @PostMapping("/best-discount")
    public ResponseEntity<ApiResponse<ValidationResponse>> bestDiscount(@RequestBody AutoApplyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Best discount result", service.autoApply(request)));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<Map<String, Object>>> redeem(@RequestBody RedeemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Redeem result", service.redeemCoupon(request)));
    }

    @PostMapping("/assign-user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assign(@RequestBody Map<String, String> request) {
        String couponCode = request.getOrDefault("couponCode", request.get("code"));
        String userId = request.get("userId");
        return ResponseEntity.ok(ApiResponse.success("Assigned", service.assignCouponToUser(couponCode, userId)));
    }
}