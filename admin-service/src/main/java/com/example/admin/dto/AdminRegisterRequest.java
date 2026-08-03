package com.example.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Main Admin registers a Sub Admin via POST /admin/register (Bearer token required).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegisterRequest {

    private String email;
    private String password;
    private String confirmPassword;

    /** Required: ORDER_SERVICE, CART_SERVICE, PAYMENT_SERVICE, USER_SERVICE, COURSE_SERVICE, INSTRUCTOR_SERVICE */
    private String assignedService;

    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String alternateMobileNumber;
}
