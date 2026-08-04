package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @JsonAlias({"mobileNumber", "mobile"})
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\d{6,12}$", message = "Mobile number must be 6-12 digits")
    private String mobileNumber;

    @NotBlank(message = "Country code is required")
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

    public List<String> getSkillsAsList() {
        if (skills == null) {
            return List.of();
        }
        if (skills instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item != null && !item.toString().trim().isEmpty())
                    .map(item -> item.toString().trim())
                    .collect(Collectors.toList());
        }
        return Arrays.stream(skills.toString().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}