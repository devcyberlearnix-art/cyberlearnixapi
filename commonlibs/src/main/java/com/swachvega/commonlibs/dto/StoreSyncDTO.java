package com.cyberlearnix.commonlibs.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class StoreSyncDTO {
    private UUID registrationId;
    private UUID merchantRegistrationId;

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
    private Boolean isOpen;

    private Double rating;
    private Integer ratingCount;
    private Integer orderCount;
    private Integer deliveryEtaMinutes;

    // Merchant documents and media
    private String merchantCertificateUrl;
    private String panCardUrl;
    private String storePhoto1Url;
    private String storePhoto2Url;
    private String storePhoto3Url;
    private String licenseUrl;

    private Map<String, Object> bankDetails;

    private LocalTime openTime;
    private LocalTime closeTime;

    // Operating hours
    private String mondayHours;
    private String tuesdayHours;
    private String wednesdayHours;
    private String thursdayHours;
    private String fridayHours;
    private String saturdayHours;
    private String sundayHours;
    private String storeTiming;
    private String[] weeklyHolidays;

    private List<String> tags;

    private Double storeScore;
    private Boolean premiumStatus;

    private String gstNumber;
    private Boolean gstRegistered;
    private String licenseNumber;
    private LocalDateTime licenseValidTill;

    private Double minOrderAmount;
    private Double deliveryFee;
    private Double freeDeliveryThreshold;
    private Double serviceRadiusKm;

    private Long viewCount;
    private Long searchCount;
    private LocalDateTime lastOrderAt;

    private LocalDateTime updatedAt;
}
