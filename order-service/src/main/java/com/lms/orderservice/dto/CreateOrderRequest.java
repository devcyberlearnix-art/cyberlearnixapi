package com.lms.orderservice.dto;

import java.util.List;

public class CreateOrderRequest {

    private String userId;
    private List<Long> courseIds;
    private Double totalAmount;
    private String couponCode;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
