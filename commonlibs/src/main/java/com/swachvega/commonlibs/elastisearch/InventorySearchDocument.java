package com.cyberlearnix.commonlibs.elastisearch;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "inventory")
public class InventorySearchDocument {

    @Id
    private String id; // Composite key: storeId-productId

    private Long storeId;
    private Long productId;
    private String gtin; // Keep for backward compatibility

    private Double price;
    private Integer stock;

    // Accept both ISO datetime (date_hour_minute_second_millis) and raw epoch_millis.
    // Spring Data ES serializes LocalDateTime as epoch_millis by default when no @Field is present,
    // which Elasticsearch rejects unless the mapping explicitly includes epoch_millis format.
    @Field(type = FieldType.Date, format = {DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis})
    private LocalDateTime lastUpdated;

    private Integer lowStockThreshold;
    private String status;
    private Double discount;
    private String offerDescription;
    private String batchNumber;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "uuuu-MM-dd||epoch_millis")
    private LocalDate expiryDate;

    // Optionally, denormalized fields for search/filter/sort
    private String storeName;
    private String merchantName;
    @Field(type = FieldType.Keyword)
    private String merchantRegistrationId;
    private String productName;
    private String category;
    private String brandName;
    private String description;
    private String shortDescription;
    private String imageUrl;
    private Boolean available;
    private String storeAddress;
    private String storeType;
    private Double originalPrice;
    private Double discountPercentage;

    // NEW ENHANCED INVENTORY FIELDS from updated Inventory entity
    @Field(type = FieldType.Integer)
    private Integer quantity;
    
    @Field(type = FieldType.Integer)
    private Integer reservedQuantity;
    
    @Field(type = FieldType.Boolean)
    private Boolean inStock;
    
    @Field(type = FieldType.Integer)
    private Integer minStockThreshold;
    
    @Field(type = FieldType.Integer)
    private Integer maxStockCapacity;
    
    @Field(type = FieldType.Integer)
    private Integer reorderPoint;
    
    @Field(type = FieldType.Integer)
    private Integer reorderQuantity;
    
    @Field(type = FieldType.Boolean)
    private Boolean availableForDelivery;
    
    @Field(type = FieldType.Boolean)
    private Boolean availableForPickup;
    
    @Field(type = FieldType.Keyword)
    private String storageLocation;
    
    @Field(type = FieldType.Text)
    private String notes;
    
    @Field(type = FieldType.Keyword)
    private String createdBy;
    
    @Field(type = FieldType.Keyword)
    private String updatedBy;

    @Field(type = FieldType.Date, format = {DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis})
    private Instant createdAt;
    
    // COMPUTED FIELDS for search optimization
    @Field(type = FieldType.Boolean)
    private Boolean isLowStock;
    
    @Field(type = FieldType.Boolean)
    private Boolean isOutOfStock;
    
    @Field(type = FieldType.Boolean)
    private Boolean hasDiscount;
    
    @Field(type = FieldType.Integer)
    private Integer availabilityScore;

    // Store location fields for proximity-based search
    @Field(type = FieldType.Double)
    private Double storeLatitude;

    @Field(type = FieldType.Double)
    private Double storeLongitude;

    // Enhanced product fields
    private BigDecimal mrp;
    private String currency;
    private String unitOfMeasure;
    private BigDecimal netContent;
    private String netContentUom;
    private BigDecimal grossWeightG;
    private String packagingType;
    private String dimensionsLwhCm;
    
    // Product classification and details
    private String manufacturerName;
    private String subCategory;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Long)
    private Long subCategoryId;
    private String countryOfOrigin;
    private String hsnCode;
    private String vegetarianNonveg;
    private String ingredients;
    private String allergens;
    private String storageInstructions;
    
    // Tax information
    private BigDecimal gstRate;
    private Boolean taxInclusive;
    private BigDecimal igstRate;
    private BigDecimal cgstRate;
    private BigDecimal sgstRate;
    
    // Product metadata
    private String sku;
    private String manufactureDate;
    private String expiryDateStr; // String version for flexible date formats
    private String batchNumberProduct; // From product, different from inventory batch
    private Integer rank;
    private String source;
    private Boolean isCustom;
    private Boolean premiumStatus;
    
    // Analytics fields
    private Long viewCount;
    private Long orderCount;
    private Double averageRating;
    private Long reviewCount;
    private Long searchCount;
    @Field(type = FieldType.Date, format = {DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis})
    private Instant lastOrderedAt;
    
    // Additional image and content fields
    private String barcodeImageUrl;
    private List<String> imageUrls;
    private Map<String, String> nutritionalInfo;
    
    // Store-related fields that are accessed in SearchServiceImpl but missing
    @Field(type = FieldType.Keyword)
    private String storePhone;
    
    @Field(type = FieldType.Double)
    private Double storeRating;
    
    @Field(type = FieldType.Long)
    private Long storeRatingCount;
    
    @Field(type = FieldType.Text)
    private String storeOperatingHours;
    
    @Field(type = FieldType.Boolean)
    private Boolean storeIsOpen;
    
    @Field(type = FieldType.Boolean)
    private Boolean storeDeliveryAvailable;
    
    @Field(type = FieldType.Double)
    private Double storeDeliveryFee;
    
    @Field(type = FieldType.Double)
    private Double storeMinOrderAmount;
    
    @Field(type = FieldType.Keyword)
    private String storeCity;
    
    @Field(type = FieldType.Keyword)
    private String storeState;
    
    @Field(type = FieldType.Keyword)
    private String storeZipCode;
    
    @Field(type = FieldType.Keyword)
    private String storeEmail;
    
    @Field(type = FieldType.Keyword)
    private String storeWebsite;
    
    @Field(type = FieldType.Keyword)
    private String storeImageUrl;
}
