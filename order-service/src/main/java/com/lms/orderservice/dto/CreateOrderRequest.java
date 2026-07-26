package com.lms.orderservice.dto;

import java.util.List;

public class CreateOrderRequest {

    private String userId;
    private List<Long> courseIds; // ✅ supports multiple courses
    private String couponCode; // optional: applied per course if valid

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

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}