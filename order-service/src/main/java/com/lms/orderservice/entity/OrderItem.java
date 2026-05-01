package com.lms.orderservice.entity;

import jakarta.persistence.*;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String orderId;
    private String courseId;

    // ✅ ADD THESE

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {   // ✅ FIX
        this.orderId = orderId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) { // ✅ FIX
        this.courseId = courseId;
    }
}