package com.lms.coupon_service.controller;

import com.lms.coupon_service.dto.CouponCreateRequest;
import com.lms.coupon_service.dto.CouponDetailsResponse;
import com.lms.coupon_service.entity.Coupon;
import com.lms.coupon_service.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CouponControllerTest {

    private MockMvc mockMvc;
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponService = Mockito.mock(CouponService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CouponController(couponService)).build();
    }

    @Test
    void createCouponReturnsCreatedApiResponse() throws Exception {
        CouponDetailsResponse response = CouponDetailsResponse.builder()
                .couponId("coupon-1")
                .code("SAVE10")
                .build();

        when(couponService.createCoupon(any(CouponCreateRequest.class), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"code\":\"SAVE10\",\"discountType\":\"PERCENTAGE\",\"discountValue\":10,\"validFrom\":\"2026-07-01T00:00:00Z\",\"validUntil\":\"2026-07-31T23:59:59Z\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Coupon created successfully."));
    }

    @Test
    void myCouponsEndpointReturnsOkResponse() throws Exception {
        when(couponService.getMyCoupons(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/coupons/my").principal(() -> "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void validateCouponByCodeReturnsDiscountValue() throws Exception {
        when(couponService.findByCode("SAVE11")).thenReturn(Coupon.builder().code("SAVE11").discountValue(15.0).build());

        mockMvc.perform(get("/api/v1/coupons/validate/SAVE11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.equalTo(15.0)));
    }
}
