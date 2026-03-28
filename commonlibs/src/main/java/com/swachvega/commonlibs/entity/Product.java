package com.cyberlearnix.commonlibs.entity;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "gtin", unique = true, nullable = false)
    private String gtin;

    private String sku;

    @Column(name = "product_name", columnDefinition = "VARCHAR(500)")
    private String productName;

    @Column(name = "brand_name", columnDefinition = "VARCHAR(255)")
    private String brandName;

    @Column(name = "manufacturer_name")
    private String manufacturerName;

    @Transient
    private String category;

    @Transient
    private String subCategory;

    // Relationship with SubCategory (optional, detailed subcategory)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SubCategory subCategoryEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_sub_category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SubSubCategory subSubCategoryEntity;

    // Primary category relationship (single category for basic categorization)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category primaryCategory;

    @Column(name = "category_id")
    private Long categoryId;

    // Many-to-many relationship with categories for advanced categorization
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "product_categories", 
        joinColumns = @JoinColumn(name = "product_id"), 
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "products", "parent", "children"})
    @org.hibernate.annotations.BatchSize(size = 25)
    private List<Category> categories = new ArrayList<>();

    private String description;

    @Column(name = "short_description")
    private String shortDescription;

    private BigDecimal mrp;

    private String currency;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "net_content")
    private BigDecimal netContent;

    @Column(name = "net_content_uom")
    private String netContentUom;

    @Column(name = "packaging_type")
    private String packagingType;

    @Column(name = "measurement_unit", length = 50)
    private String measurementUnit;

    @Column(name = "dimension_unit", length = 50)
    private String dimensionUnit;

    @Column(name = "dimension_width")
    private BigDecimal dimensionWidth;

    @Column(name = "dimension_height")
    private BigDecimal dimensionHeight;

    @Column(name = "dimension_depth")
    private BigDecimal dimensionDepth;

    @Column(name = "dimensions_lwh_cm")
    private String dimensionsLwhCm;

    @Column(name = "gross_weight_g")
    private BigDecimal grossWeightG;

    @Column(name = "gross_weight", length = 100)
    private String grossWeight;

    @Column(name = "net_weight", length = 100)
    private String netWeight;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "hs_code", length = 50)
    private String hsCode;

    @Column(name = "gst_rate")
    private BigDecimal gstRate;

    @Column(name = "tax_inclusive")
    private Boolean taxInclusive;

    @Column(name = "igst_rate")
    private BigDecimal igstRate;

    @Column(name = "cgst_rate")
    private BigDecimal cgstRate;

    @Column(name = "sgst_rate")
    private BigDecimal sgstRate;

    @Column(name = "igst", precision = 10, scale = 2)
    private BigDecimal igst;

    @Column(name = "cgst", precision = 10, scale = 2)
    private BigDecimal cgst;

    @Column(name = "sgst", precision = 10, scale = 2)
    private BigDecimal sgst;

    @Column(name = "shelf_life_value")
    private Integer shelfLifeValue;

    @Column(name = "shelf_life_unit", length = 50)
    private String shelfLifeUnit;

    @Column(name = "shelf_life_based_on", length = 100)
    private String shelfLifeBasedOn;

    @Column(name = "storage_condition", length = 200)
    private String storageCondition;

    @Column(name = "caution_warning", columnDefinition = "TEXT")
    private String cautionWarning;

    @Column(name = "activation_date")
    private LocalDate activationDate;

    @Column(name = "deactivation_date")
    private LocalDate deactivationDate;

    @Column(name = "fssai_license_no", length = 500)
    private String fssaiLicenseNo;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_image_urls", joinColumns = @JoinColumn(name = "gtin"))
    @Column(name = "image_url")
    @org.hibernate.annotations.BatchSize(size = 25)
    private List<String> imageUrls;

    @Column(name = "barcode_image_url")
    private String barcodeImageUrl;

    @Column(name = "front_image_url", length = 500)
    private String frontImageUrl;

    @Column(name = "back_image_url", length = 500)
    private String backImageUrl;

    @Column(name = "top_image_url", length = 500)
    private String topImageUrl;

    @Column(name = "bottom_image_url", length = 500)
    private String bottomImageUrl;

    @Column(name = "left_image_url", length = 500)
    private String leftImageUrl;

    @Column(name = "right_image_url", length = 500)
    private String rightImageUrl;

    @Column(name = "artwork_front_image_url", length = 500)
    private String artworkFrontImageUrl;

    @Column(name = "artwork_back_image_url", length = 500)
    private String artworkBackImageUrl;

    @ElementCollection
    @CollectionTable(name = "product_nutritional_info", joinColumns = @JoinColumn(name = "gtin"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> nutritionalInfo;

    private String ingredients;

    private String allergens;

    @Column(name = "vegetarian_nonveg")
    private String vegetarianNonveg;

    @Column(name = "storage_instructions")
    private String storageInstructions;

    @Column(name = "manufacture_date")
    private String manufactureDate;

    @Column(name = "expiry_date")
    private String expiryDate;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "primary_gtin", length = 50)
    private String primaryGtin;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "marketing_info", columnDefinition = "TEXT")
    private String marketingInfo;

    @Column(name = "derived_description", columnDefinition = "TEXT")
    private String derivedDescription;

    @Column(name = "brand", length = 255)
    private String brand;

    @Column(name = "name", length = 500)
    private String name;

    @Column(name = "company_name", length = 500)
    private String companyName;

    // Company address fields (DataKart compatibility)
    @Column(name = "company_address1", length = 500)
    private String companyAddress1;

    @Column(name = "company_address2", length = 500)
    private String companyAddress2;

    @Column(name = "company_city", length = 100)
    private String companyCity;

    @Column(name = "company_state", length = 100)
    private String companyState;

    @Column(name = "company_pincode", length = 20)
    private String companyPincode;

    @Column(name = "company_country", length = 100)
    private String companyCountry;

    // Age group attribute (for baby products, etc.)
    @Column(name = "age_group", length = 100)
    private String ageGroup;

    // MRP target market fields (DataKart compatibility)
    @Column(name = "mrp_target_market", length = 100)
    private String mrpTargetMarket;

    @Column(name = "mrp_location", length = 255)
    private String mrpLocation;

    @Column(name = "mrp_activation_date")
    private LocalDate mrpActivationDate;

    // DataKart sync tracking dates
    @Column(name = "datakart_created_date")
    private java.time.LocalDateTime datakartCreatedDate;

    @Column(name = "datakart_modified_date")
    private java.time.LocalDateTime datakartModifiedDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "case_configuration", columnDefinition = "jsonb")
    private JsonNode caseConfiguration;

    private Integer rank;

    @Column(name = "gtin_status")
    private String gtinStatus;

    @Column(name = "last_updated")
    private String lastUpdated;

    @Column(name = "source", length = 50)
    private String source; // e.g., "DATAKART", "STORE"

    @Column(name = "created_by_store_id")
    private Long createdByStoreId; // If custom, which store created it

    @Column(name = "merchant_id")
    private String merchantId; // Merchant/user identifier who created it

    @Column(name = "is_custom")
    private Boolean isCustom = false;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "reason_for_rejection")
    private String reasonForRejection;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    // Analytics fields for trending and popularity
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "order_count")
    private Long orderCount = 0L;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    private Long reviewCount = 0L;

    @Column(name = "search_count")
    private Long searchCount = 0L;

    @Column(name = "last_ordered_at")
    private java.time.LocalDateTime lastOrderedAt;

    // REMOVED: availableLocations field - product availability is now managed
    // through Inventory service
    // Product availability is determined by checking inventory across stores, not
    // stored in product entity

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    // New field for premium status, defaulting to false
    @Column(name = "premium_status")
    private Boolean premiumStatus = false;

    @Transient
    private Double relevanceScore;

    @Transient
    private Double storeRecommendationScore;

    // NOTE: Stock information is handled by Inventory service
    // Individual stores maintain their own inventory levels
    // Product availability is determined by querying inventory across stores

    // Existing manual getters/setters (if any) remain, Lombok covers the rest
    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    // Helper methods for weight calculations
    public Double getWeight() {
        if (grossWeightG != null) {
            return grossWeightG.doubleValue() / 1000.0; // Convert grams to kilograms
        }
        return 0.0;
    }

    public String getWeightUnit() {
        return "kg"; // Always return kg as we convert from grams
    }

    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0); // Return first image URL
        }
        return null;
    }
}
