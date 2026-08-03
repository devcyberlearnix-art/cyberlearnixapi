package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String confirmPassword; // ✅ Add this

    private String firstName;
    private String lastName;
    @JsonAlias({"mobileNumber", "mobile"})
    private String mobileNumber;
    private String countryCode;
    private String dob;
    private String profilePhoto;
    private String city;
    private String state;
    private String country;
    private String preferredLanguage;
    private String organization;
    private Object skills;
    private String fieldOfStudy;
    private String highestQualification;

    public String getSkillsAsString() {
        if (skills == null) {
            return null;
        }
        if (skills instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item != null && !item.toString().trim().isEmpty())
                    .map(item -> item.toString().trim())
                    .reduce((left, right) -> left + "," + right)
                    .orElse(null);
        }
        String scalar = skills.toString().trim();
        return scalar.isEmpty() ? null : scalar;
    }
}