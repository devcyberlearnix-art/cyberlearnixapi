package com.example.admin.dto;


import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {
    private String status;
    private boolean appliedForInstructor;  // true if user applied
    private UUID id;
    private String email;
    private String role;
    private String createdAt;

}
