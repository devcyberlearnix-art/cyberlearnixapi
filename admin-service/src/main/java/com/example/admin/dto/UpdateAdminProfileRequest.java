package com.example.admin.dto;

import lombok.Data;

@Data
public class UpdateAdminProfileRequest {

    private String email;
    private String password;
}