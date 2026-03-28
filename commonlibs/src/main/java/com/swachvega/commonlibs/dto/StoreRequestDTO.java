package com.cyberlearnix.commonlibs.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class StoreRequestDTO {
    private Long storeId; // For update scenarios
    private String storeName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private String email;
    private String website;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private Boolean isActive;
    private Boolean isTrending;
    private Double rating;
    private Integer ratingCount;
    private Integer orderCount;
    private List<String> categories; // e.g., ["groceries", "bakery"] - Legacy support
    private List<Long> categoryIds; // Category entity IDs - Preferred approach
    private List<String> tags; // e.g., ["organic", "24x7"]

    private Boolean premiumStatus;
    private Boolean isOpen;
    private Integer deliveryEtaMinutes;
    private Double storeScore;

    // GST and License fields
    private String gstNumber;
    private Boolean gstRegistered;
    private String licenseNumber;
    private LocalDateTime licenseValidTill;

    // Merchant linkage and documents
    private UUID registrationId; // UUID
    private UUID merchantRegistrationId; // UUID
    private String merchantCertificateUrl;
    private String panCardUrl;
    private String storePhoto1Url;
    private String storePhoto2Url;
    private String storePhoto3Url;
    private String licenseUrl;
    private Map<String, Object> bankDetails;

    // Operating hours (per-day, and textual timing)
    private String mondayHours;
    private String tuesdayHours;
    private String wednesdayHours;
    private String thursdayHours;
    private String fridayHours;
    private String saturdayHours;
    private String sundayHours;
    private String storeTiming;
    private String[] weeklyHolidays;

    // Opening/closing times as strings (HH:mm)
    private String openingTime;
    private String closingTime;
    // Alternate field names accepted by some clients
    private String openTime;
    private String closeTime;

    // Additional store metadata
    private String storeType;
    private Double minOrderAmount;
    private Double deliveryFee;
    private Double freeDeliveryThreshold;
    private Double serviceRadiusKm;
    private Long viewCount;
    private Long searchCount;
    private LocalDateTime lastOrderAt;
}
