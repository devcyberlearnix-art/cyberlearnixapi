package com.swachvega.apigateway.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber == null || phoneNumber.trim().isEmpty() || 
               PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static String validateRegistrationData(String username, String email, 
                                                 String password, String confirmPassword,
                                                 boolean acceptTerms, boolean acceptPrivacy) {
        if (!isValidUsername(username)) {
            return "Username must be 3-50 characters long and contain only letters, numbers, and underscores";
        }
        
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }
        
        if (!isValidPassword(password)) {
            return "Password must be at least 8 characters long and contain uppercase, lowercase, and number";
        }
        
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        
        if (!acceptTerms) {
            return "You must accept the terms and conditions";
        }
        
        if (!acceptPrivacy) {
            return "You must accept the privacy policy";
        }
        
        return null; // Valid
    }

    public static String validateLoginData(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "Username or email is required";
        }
        
        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }
        
        return null; // Valid
    }
}
