package com.swachvega.apigateway.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRequest {
    // User identification
    private String name;
    private String email;
    private String phone;
    
    // OTP validation
    private String otp;
    private String otpSessionId; // To track OTP session
    
    // Device information
    private String deviceId;
    private String deviceName;

    public AuthRequest() {}

    public AuthRequest(String name, String email, String phone, String otp, String otpSessionId, String deviceId, String deviceName) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.otp = otp;
        this.otpSessionId = otpSessionId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }
}
