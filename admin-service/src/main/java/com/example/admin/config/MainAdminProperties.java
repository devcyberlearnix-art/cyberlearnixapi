package com.example.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "main.admin")
public class MainAdminProperties {

    private String email = "mainadmin@cyberlearnix.com";
    private String password = "MainAdmin@123";
    private String firstName = "Main";
    private String lastName = "Admin";
    private String mobileNumber = "+911234567890";
}
