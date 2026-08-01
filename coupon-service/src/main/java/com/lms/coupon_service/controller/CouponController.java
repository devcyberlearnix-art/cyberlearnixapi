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
    @PreAuthorize("hasAuthority('SERVICE_COUPON_SERVICE') or hasAuthority('SERVICE_ALL') or hasAnyRole('MAIN_ADMIN','SUB_ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> create(@RequestBody CouponCreateRequest req) {

        CouponDetailsResponse details = service.createCoupon(
                req,
                currentUserId(),
                currentUserRole());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully.", details));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('SERVICE_COUPON_SERVICE') or hasAuthority('SERVICE_ALL') or hasAnyRole('MAIN_ADMIN','SUB_ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<BulkResponse>> bulk(@RequestBody BulkRequest request) {

        if (request.getCreatorRole() == null || request.getCreatorRole().isBlank()) {
            request.setCreatorRole("MAIN_ADMIN");
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
            return ResponseEntity.ok(
                    coupon != null && coupon.isActive()
                            ? coupon.getDiscountValue()
                            : 0.0);
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(0.0);
        }
    }

    @PutMapping("/{couponId}")
    @PreAuthorize("hasAuthority('SERVICE_COUPON_SERVICE') or hasAuthority('SERVICE_ALL') or hasAnyRole('MAIN_ADMIN','SUB_ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> update(
            @PathVariable String couponId,
            @RequestBody CouponCreateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Coupon updated successfully.",
                        service.updateCoupon(
                                couponId,
                                request,
                                currentUserId(),
                                currentUserRole()
                        )));
    }

    @DeleteMapping("/{couponId}")
    @PreAuthorize("hasAuthority('SERVICE_COUPON_SERVICE') or hasAuthority('SERVICE_ALL') or hasAnyRole('MAIN_ADMIN','SUB_ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable String couponId) {

        service.deleteById(couponId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Coupon deleted successfully",
                        Map.of("couponId", couponId)));
    }

    @PatchMapping("/{couponId}/activate")
    @PreAuthorize("hasAuthority('SERVICE_COUPON_SERVICE') or hasAuthority('SERVICE_ALL') or hasAnyRole('MAIN_ADMIN','SUB_ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> activate(@PathVariable String couponId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Coupon activated successfully.",
                        service.activateCoupon(couponId)));
    }

    @PatchMapping("/{couponId}/deactivate")
    @PreAuthorize("hasAuthority('SERVICE_COUPON_SERVICE') or hasAuthority('SERVICE_ALL') or hasAnyRole('MAIN_ADMIN','SUB_ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CouponDetailsResponse>> deactivate(@PathVariable String couponId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Coupon deactivated successfully.",
                        service.deactivateCoupon(couponId)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CouponDetailsResponse>>> getMyCoupons() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "My coupons",
                        service.getMyCoupons(currentUserId())));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<List<String>>> getCampaigns() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Campaigns retrieved successfully.",
                        service.getCampaignNames()));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ValidationResponse>> validate(
            @RequestBody ValidateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Validation result",
                        service.validateCoupon(request, currentUserId())));
    }

    @PostMapping("/best-discount")
    public ResponseEntity<ApiResponse<ValidationResponse>> bestDiscount(
            @RequestBody AutoApplyRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Best discount result",
                        service.autoApply(request, currentUserId())));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<Map<String, Object>>> redeem(
            @RequestBody RedeemRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Redeem result",
                        service.redeemCoupon(request, currentUserId())));
    }

    @PostMapping("/assign-user")
    @PreAuthorize("hasAuthority('SERVICE_COUPON_SERVICE') or hasAuthority('SERVICE_ALL') or hasAnyRole('MAIN_ADMIN','SUB_ADMIN','INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assign(
            @RequestBody Map<String, String> request) {

        String couponCode = request.getOrDefault("couponCode", request.get("code"));
        String userId = request.get("userId");

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Assigned",
                        service.assignCouponToUser(couponCode, userId)));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    private String currentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return "UNKNOWN";
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse("UNKNOWN");
    }
}