package com.example.admin.dto;


import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    private String status;// ACTIVE / INACTIVE
    private String applicationStatus; // APPROVED, REJECTED

}