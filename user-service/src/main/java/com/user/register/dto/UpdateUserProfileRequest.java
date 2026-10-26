package com.user.register.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name can only contain letters and spaces")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Last name can only contain letters and spaces")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String mobile;

    private String countryCode;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
    private String dob;

    private String profilePhoto;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Pattern(regexp = "^(EN|ES|FR|DE)$", message = "Preferred language must be one of: EN, ES, FR, DE")
    private String preferredLanguage;

    @Size(max = 200, message = "Organization must not exceed 200 characters")
    private String organization;

    @Size(max = 500, message = "Skills must not exceed 500 characters")
    private String skills;

    @Size(max = 200, message = "Field of study must not exceed 200 characters")
    private String fieldOfStudy;

    @Size(max = 200, message = "Highest qualification must not exceed 200 characters")
    private String highestQualification;

    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be one of: Male, Female, Other")
    private String gender;

    public boolean isEmpty() {
        return firstName == null && lastName == null && email == null && mobile == null
                && countryCode == null && dob == null && profilePhoto == null && city == null
                && state == null && country == null && preferredLanguage == null
                && organization == null && skills == null && fieldOfStudy == null
                && highestQualification == null && gender == null;
    }
}
