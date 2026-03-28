package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long productId;              // Internal product ID
    private String gtin;
    private String sku;
    private String productName;
    private String brandName;
    private String manufacturerName;
    private String category;
    private String subCategory;
    
    // Category information
    private Long primaryCategoryId;
    private String primaryCategoryName;
    private String primaryCategoryDisplayName;
    private List<CategoryInfo> categories;   // All categories this product belongs to

    private Long subCategoryId;
    private String subCategoryName;
    private String subCategoryDisplayName;
    private List<SubCategoryInfo> subCategories; // Subcategory details (for UI)

    private Long subSubCategoryId;
    private String subSubCategoryName;
    private String subSubCategoryDisplayName;
    
    private String description;
    private String shortDescription;
    private BigDecimal mrp;
    private String currency;
    private String unitOfMeasure;
    private BigDecimal netContent;
    private String netContentUom;
    private String packagingType;
    private String measurementUnit;
    private String dimensionUnit;
    private BigDecimal dimensionWidth;
    private BigDecimal dimensionHeight;
    private BigDecimal dimensionDepth;
    private String dimensionsLwhCm;
    private BigDecimal grossWeightG;
    private String grossWeight;
    private String netWeight;
    private String countryOfOrigin;
    private String hsnCode;
    private String hsCode;
    private BigDecimal gstRate;
    private Boolean taxInclusive;
    private BigDecimal igstRate;
    private BigDecimal cgstRate;
    private BigDecimal sgstRate;
    private BigDecimal igst;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private Integer shelfLifeValue;
    private String shelfLifeUnit;
    private String shelfLifeBasedOn;
    private String storageCondition;
    private String cautionWarning;
    private LocalDate activationDate;
    private LocalDate deactivationDate;
    private String fssaiLicenseNo;
    private List<String> imageUrls;
    private String barcodeImageUrl;
    private String frontImageUrl;
    private String backImageUrl;
    private String topImageUrl;
    private String bottomImageUrl;
    private String leftImageUrl;
    private String rightImageUrl;
    private String artworkFrontImageUrl;
    private String artworkBackImageUrl;
    private Map<String, String> nutritionalInfo;
    private String ingredients;
    private String allergens;
    private String vegetarianNonveg;
    private String storageInstructions;
    private String manufactureDate;
    private String expiryDate;
    private String batchNumber;
    private String primaryGtin;
    private String url;
    private String marketingInfo;
    private String derivedDescription;
    private String brand;
    private String name;
    private String companyName;
    
    // Company address fields (DataKart compatibility)
    private String companyAddress1;
    private String companyAddress2;
    private String companyCity;
    private String companyState;
    private String companyPincode;
    private String companyCountry;
    
    // Age group (DataKart compatibility)
    private String ageGroup;
    
    // MRP target market fields (DataKart compatibility)
    private String mrpTargetMarket;
    private String mrpLocation;
    private java.time.LocalDate mrpActivationDate;
    
    private String caseConfiguration;
    private Integer rank;
    private String gtinStatus;
    private String lastUpdated;

    // For custom/official product support
    private String source; // "DATAKART" or "STORE"
    private Boolean isCustom;
    private Long createdByStoreId;
    private String merchantId;      // Merchant user identifier
    private String registrationId;  // Alias for merchant id if needed by clients
    private String approvalStatus;
    private String reasonForRejection;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // Analytics fields for trending and popularity
    private Long viewCount = 0L;
    private Long orderCount = 0L;
    private Double averageRating = 0.0;
    private Long reviewCount = 0L;
    private Long searchCount = 0L;
    private java.time.LocalDateTime lastOrderedAt;

    // Location-based fields
    private List<String> availableLocations;

    // Computed fields for response
    private Double currentPrice;
    private String nearestStoreDistance;
    private List<String> nearbyStores;
    
    // Inventory-specific fields (populated when storeId is provided)
    private Double discountPercentage;
    private String offerDescription;
    private Integer stock;
    private Boolean inStock;
}
