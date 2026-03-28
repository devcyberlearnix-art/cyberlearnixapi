package com.cyberlearnix.commonlibs.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@JsonInclude(Include.NON_NULL)
public class StoreResponseDTO {
    private Long storeId;
    @JsonIgnore
    private java.util.UUID registrationId;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalTime openTime;
    private LocalTime closeTime;

    // Merchant linkage and documents
    private UUID merchantRegistrationId;
    private String merchantCertificateUrl;
    private String panCardUrl;
    private String storePhoto1Url;
    private String storePhoto2Url;
    private String storePhoto3Url;
    private String licenseUrl;
    private Map<String, Object> bankDetails;

    // Category relationships
    private List<CategoryResponseDTO> categories;
    private List<String> categoryStrings; // Legacy support - will be deprecated
    private List<Long> categoryIds; // For ID-based category responses

    private List<String> tags;
    private Double storeScore;
    private Boolean premiumStatus;
    private Boolean isOpen;
    private Integer deliveryEtaMinutes;
    private String deliveryEtaText;
    private String deliveryTime;
    private String gstNumber;
    private Boolean gstRegistered;
    private String licenseNumber;
    private LocalDateTime licenseValidTill;

    // Operating hours (string-based and per-day)
    private String mondayHours;
    private String tuesdayHours;
    private String wednesdayHours;
    private String thursdayHours;
    private String fridayHours;
    private String saturdayHours;
    private String sundayHours;
    private String storeTiming;
    private String[] weeklyHolidays;

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
