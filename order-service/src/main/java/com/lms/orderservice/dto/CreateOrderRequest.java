package com.lms.orderservice.dto;

import java.util.List;

public class CreateOrderRequest {

    private String userId;
    private List<String> courseIds; // ✅ supports multiple courses
    private String couponCode; // optional: applied per course if valid

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}