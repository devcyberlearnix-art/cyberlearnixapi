package com.example.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserServiceResponse {

    private boolean success;
    private String message;
    private String timestamp;
    private List<UserResponse> data;

}
