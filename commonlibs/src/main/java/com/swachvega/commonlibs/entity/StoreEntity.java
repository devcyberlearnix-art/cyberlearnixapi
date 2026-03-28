package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.util.Map;
 

@Setter
@Getter
@Entity
@Table(name = "stores", indexes = {
        @Index(name = "idx_store_name", columnList = "storeName"),
        @Index(name = "idx_store_location", columnList = "latitude,longitude"),
        @Index(name = "idx_stores_vertical", columnList = "store_vertical"),
        @Index(name = "idx_stores_group", columnList = "group_id")
})
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    @Column(nullable = false, unique = true, length = 100)
    private String storeName;

    @Column(length = 100)
    private String businessName;

    @Column(length = 15, unique = false)
    private String mobileNumber;

    @Column(length = 255)
    private String alternateContact;

    @Column
    private String passwordHash;

    @Column(length = 255)
    private String description;

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String houseNumber;

    @Column(length = 100)
    private String street;

    @Column(length = 100)
    private String area;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String zipCode;

    @Column(length = 6)
    private String pincode;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isTrending = false;

    public enum RegistrationStep {
        SIGNUP_PENDING,
        OTP_VERIFICATION_PENDING,
        PASSWORD_PENDING,
        ADDRESS_PENDING,
        GST_AND_BANK_DETAILS_PENDING,
        DOCUMENTS_PENDING,
        COMPLETED
    }

    @Enumerated(EnumType.STRING)
    @Column
    private RegistrationStep currentStep;

    @Column
    private Boolean mobileVerified;

    @Column
    private Boolean emailVerified;

    // Store rating; we round to 1 decimal in service code before saving
    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private Integer ratingCount = 0;

    @Column(nullable = false)
    private Integer orderCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // External registration identifier (UUID)
//    @Column(name = "registration_id", nullable = true, unique = true)
//    private UUID registrationId;

    // Link to MerchantRegistrationEntity (marchant_users)
    @Column(name = "merchant_registration_id", nullable = true, unique = true)
    private UUID merchantRegistrationId;

    // Merchant documents and media
    @Column(name = "merchant_certificate_url", length = 255)
    private String merchantCertificateUrl;

    @Column(name = "pan_card_url", length = 255)
    private String panCardUrl;

    @Column(name = "aadhaar_card_url", length = 255)
    private String aadhaarCardUrl;

    @Column(name = "store_photo1_url", length = 255)
    private String storePhoto1Url;

    @Column(name = "store_photo2_url", length = 255)
    private String storePhoto2Url;

    @Column(name = "store_photo3_url", length = 255)
    private String storePhoto3Url;

    @Column(name = "license_url", length = 255)
    private String licenseUrl;

    // Bank details JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bank_details", columnDefinition = "jsonb")
    private Map<String, Object> bankDetails;

    // Store open and close times
    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column
    private Boolean storeClosed;

    @Column(length = 10)
    private String openingTime;

    @Column(length = 10)
    private String closingTime;

    // Example: categories offered by the store (using Category entities instead of
    // strings)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "store_categories", joinColumns = @JoinColumn(name = "store_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories = new ArrayList<>();

    // Legacy support - will be deprecated in favor of Category entities above
    @ElementCollection
    @CollectionTable(name = "store_category_strings", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "category")
    private List<String> categoryStrings;

    @Column(name = "category_strings", columnDefinition = "text[]")
    private String[] categoryStringsArray;

    // Example: tags for recommendations/trending (e.g., "organic", "24x7",
    // "discounts")
    @ElementCollection
    @CollectionTable(name = "store_tags", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "tags_array", columnDefinition = "text[]")
    private String[] tagsArray;

    @Column(name = "store_score")
    private Double storeScore;

    @Column(name = "premium_status")
    private Boolean premiumStatus = false;

    @Column(name = "is_open")
    private Boolean isOpen = true;

    @Column(name = "delivery_eta_minutes")
    private Integer deliveryEtaMinutes;

    // Human-friendly delivery ETA text, e.g., "2 hours 20 minutes"
    @Column(name = "delivery_eta_text", length = 100)
    private String deliveryEtaText;

    @Column(name = "delivery_hours")
    private Integer deliveryHours;

    @Column(name = "delivery_minutes")
    private Integer deliveryMinutes;

    @Column(name = "delivery_duration_iso", length = 32)
    private String deliveryDurationIso;

    @Column(name = "delivery_duration_text", length = 64)
    private String deliveryDurationText;

    @Column(name = "gst_number", length = 15)
    private String gstNumber; // GSTIN (15 chars)

    @Column(name = "gst_registered")
    private Boolean gstRegistered = false;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "license_valid_till")
    private LocalDateTime licenseValidTill;

    // Analytics fields
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "search_count")
    private Long searchCount = 0L;

    @Column(name = "last_order_at")
    private LocalDateTime lastOrderAt;

    // Operating hours
    @Column(name = "monday_hours")
    private String mondayHours;

    @Column(name = "tuesday_hours")
    private String tuesdayHours;

    @Column(name = "wednesday_hours")
    private String wednesdayHours;

    @Column(name = "thursday_hours")
    private String thursdayHours;

    @Column(name = "friday_hours")
    private String fridayHours;

    @Column(name = "saturday_hours")
    private String saturdayHours;

    @Column(name = "sunday_hours")
    private String sundayHours;

    @Column(length = 100)
    private String storeTiming;

    @Column(columnDefinition = "text[]")
    private String[] weeklyHolidays;

    // Additional store metadata
    @Column(name = "store_type")
    private String storeType; // "GROCERY", "PHARMACY", "RESTAURANT", etc.

    @Column(name = "min_order_amount")
    private Double minOrderAmount;

    @Column(name = "delivery_fee")
    private Double deliveryFee;

    @Column(name = "free_delivery_threshold")
    private Double freeDeliveryThreshold;

    @Column(name = "service_radius_km")
    private Double serviceRadiusKm;

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approval_message", length = 255)
    private String approvalMessage;

    @Column(name = "approval_updated_at")
    private OffsetDateTime approvalUpdatedAt;

    // Restaurant vertical fields
    @Column(name = "store_vertical", length = 20)
    private String storeVertical; // "GROCERY" | "RESTAURANT"

    @Column(name = "is_accepting_orders")
    private Boolean isAcceptingOrders = true;

    @Column(name = "group_id")
    private Long groupId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Category Helper Methods ---

    public void addCategory(Category category) {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    public void removeCategory(Category category) {
        if (categories != null) {
            categories.remove(category);
        }
    }

    public boolean hasCategory(Category category) {
        return categories != null && categories.contains(category);
    }

    public boolean hasCategoryById(Long categoryId) {
        return categories != null && categories.stream()
                .anyMatch(cat -> cat.getId().equals(categoryId));
    }

    public List<Long> getCategoryIds() {
        if (categories == null) {
            return new ArrayList<>();
        }
        return categories.stream()
                .map(Category::getId)
                .toList();
    }

    /**
     * Dynamically calculates if the store is currently open based on:
     * 1. Opening and closing times
     * 2. Weekly holidays
     * 3. Current IST (Indian Standard Time) time and day
     * 
     * @return true if store should be open now, false otherwise
     */
    public boolean calculateIsOpen() {
        // If store is marked as inactive, it's closed
        if (this.isActive != null && !this.isActive) {
            return false;
        }
        
        // If no opening/closing times are set, default to open
        if (this.openingTime == null || this.closingTime == null) {
            // Fallback to the stored isOpen value if available
            return this.isOpen != null ? this.isOpen : true;
        }
        
        try {
            // Use IST timezone via utility class
            java.time.LocalTime now = com.cyberlearnix.commonlibs.util.ISTTimeUtil.now();
            java.time.LocalTime opening = java.time.LocalTime.parse(this.openingTime);
            java.time.LocalTime closing = java.time.LocalTime.parse(this.closingTime);
            
            // Check if today is a holiday (in IST)
            java.time.DayOfWeek today = com.cyberlearnix.commonlibs.util.ISTTimeUtil.currentDayOfWeek();
            if (this.weeklyHolidays != null) {
                for (String holiday : this.weeklyHolidays) {
                    if (holiday != null && holiday.equalsIgnoreCase(today.name())) {
                        return false; // Closed on weekly holiday
                    }
                }
            }
            
            // Check if current time is within operating hours
            return !now.isBefore(opening) && !now.isAfter(closing);
            
        } catch (Exception e) {
            // If parsing fails, fallback to stored isOpen value
            return this.isOpen != null ? this.isOpen : true;
        }
    }

    // --- Getters and Setters ---

}
