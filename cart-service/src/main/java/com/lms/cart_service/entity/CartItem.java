package com.lms.cart_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cart_user_course", columnNames = {"user_id", "course_id"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;       // Extracted from JWT Token

    @Column(name = "instructor_id")
    private String instructorId; // From Request Body

    @Column(name = "course_id")
    private Long courseId;     // From Request Body

    @Column(name = "course_name")
    private String courseName;

    private Double price;
    private Integer quantity;    // Used for the "Minase" (Minus) logic
    private String couponCode;   // Applied coupon code for this cart
}
