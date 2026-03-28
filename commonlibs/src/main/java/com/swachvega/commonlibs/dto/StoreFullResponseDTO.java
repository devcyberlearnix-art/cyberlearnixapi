package com.cyberlearnix.commonlibs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@JsonInclude(Include.NON_NULL)
public class StoreFullResponseDTO {
    // Identity
    private Long storeId;
    private String storeName;
    private String businessName;

    // Contacts
    private String mobileNumber;
    private String alternateContact;
    private String phone;
    private String email;
    private Boolean mobileVerified;
    private Boolean emailVerified;

    // Address
    private String address;
    private String houseNumber;
    private String street;
    private String area;
    private String city;
    private String state;
    private String zipCode;
    private String pincode;
    private Double latitude;
    private Double longitude;

    // Meta
    private String description;
    private String website;
    private String imageUrl;
    private Boolean isActive;
    private Boolean isTrending;
    private Boolean isOpen;
    private Boolean storeClosed;
    private Boolean premiumStatus;

    // Registration / Approval
    private UUID merchantRegistrationId;
    private String approvalStatus;
    private String approvalMessage;
    private OffsetDateTime approvalUpdatedAt;
    private String currentStep;

    // Ratings / Stats
    private Double rating;
    private Integer ratingCount;
    private Integer orderCount;
    private Long viewCount;
    private Long searchCount;
    private LocalDateTime lastOrderAt;

    // Times
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String openingTime;
    private String closingTime;

    // Delivery
    private Integer deliveryEtaMinutes;
    private String deliveryEtaText;
    private Integer deliveryHours;
    private Integer deliveryMinutes;
    private String deliveryDurationIso;
    private String deliveryDurationText;

    // Documents
    private String merchantCertificateUrl;
    private String panCardUrl;
    private String storePhoto1Url;
    private String storePhoto2Url;
    private String storePhoto3Url;
    private String licenseUrl;
    private String licenseNumber;
    private LocalDateTime licenseValidTill;

    // Financial
    private Map<String, Object> bankDetails;

    // Operating (strings)
    private String mondayHours;
    private String tuesdayHours;
    private String wednesdayHours;
    private String thursdayHours;
    private String fridayHours;
    private String saturdayHours;
    private String sundayHours;
    private String storeTiming;
    private String[] weeklyHolidays;

    // Categories / Tags
    private List<CategoryResponseDTO> categories;
    private List<String> categoryStrings;
    private List<Long> categoryIds;
    private List<String> tags;

    // Additional metadata
    private String storeType;
    private Double minOrderAmount;
    private Double deliveryFee;
    private Double freeDeliveryThreshold;
    private Double serviceRadiusKm;
    private Double storeScore;

    // Distance & ETA info (populated when user lat/lng provided)
    private DistanceInfo distance;

    @Data
    @JsonInclude(Include.NON_NULL)
    public static class DistanceInfo {
        private Double distanceKm;
        private String distanceText;
        private String estimatedDeliveryTime;
        private Integer preparationTimeMinutes;
        private String preparationTimeText;
        private Boolean isDeliverable;
        private Boolean isNearestStore;
        private Integer storeRank;
    }
}
