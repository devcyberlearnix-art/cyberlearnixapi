package com.cyberlearnix.commonlibs.elastisearch;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.cyberlearnix.commonlibs.util.FlexibleLocalDateTimeDeserializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "products")
public class ProductSearchDocument {
    @Id
    private String gtin;
    
    @Field(type = FieldType.Long)
    private Long productId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String sku;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String productName;
    
    @Field(type = FieldType.Keyword)
    private String brandName;
    
    @Field(type = FieldType.Text)
    private String manufacturerName;
    
    @Field(type = FieldType.Keyword)
    private List<String> category;

    @Field(type = FieldType.Keyword)
    private String primaryCategory;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Text)
    private String shortDescription;
    
    @Field(type = FieldType.Double)
    private BigDecimal mrp;
    
    @Field(type = FieldType.Keyword)
    private String currency;
    
    @Field(type = FieldType.Keyword)
    private String unitOfMeasure;
    
    @Field(type = FieldType.Double)
    private BigDecimal netContent;
    
    @Field(type = FieldType.Keyword)
    private String netContentUom;
    
    @Field(type = FieldType.Keyword)
    private String packagingType;
    
    @Field(type = FieldType.Keyword)
    private String dimensionsLwhCm;
    
    @Field(type = FieldType.Double)
    private BigDecimal grossWeightG;
    
    @Field(type = FieldType.Keyword)
    private String countryOfOrigin;
    
    @Field(type = FieldType.Keyword)
    private String hsnCode;
    
    @Field(type = FieldType.Double)
    private BigDecimal gstRate;
    
    @Field(type = FieldType.Boolean)
    private Boolean taxInclusive;
    
    @Field(type = FieldType.Double)
    private BigDecimal igstRate;
    
    @Field(type = FieldType.Double)
    private BigDecimal cgstRate;
    
    @Field(type = FieldType.Double)
    private BigDecimal sgstRate;
    
    @Field(type = FieldType.Keyword)
    private List<String> imageUrls;
    
    @Field(type = FieldType.Keyword)
    private String barcodeImageUrl;
    
    @Field(type = FieldType.Keyword)
    private String frontImageUrl;
    
    @Field(type = FieldType.Keyword)
    private String fssaiLicenseNo;
    
    @Field(type = FieldType.Keyword)
    private String netWeight;
    
    @Field(type = FieldType.Keyword)
    private String grossWeight;
    
    @Field(type = FieldType.Keyword)
    private String ageGroup;
    
    @Field(type = FieldType.Integer)
    private Integer shelfLifeValue;
    
    @Field(type = FieldType.Keyword)
    private String shelfLifeUnit;
    
    @Field(type = FieldType.Object)
    private Map<String, String> nutritionalInfo;
    
    @Field(type = FieldType.Text)
    private String ingredients;
    
    @Field(type = FieldType.Text)
    private String allergens;
    
    @Field(type = FieldType.Keyword)
    private String vegetarianNonveg;
    
    @Field(type = FieldType.Text)
    private String storageInstructions;
    
    @Field(type = FieldType.Keyword)
    private String manufactureDate;
    
    @Field(type = FieldType.Keyword)
    private String expiryDate;
    
    @Field(type = FieldType.Keyword)
    private String batchNumber;
    
    @Field(type = FieldType.Integer)
    private Integer rank;
    
    @Field(type = FieldType.Keyword)
    private String gtinStatus;
    
    @Field(type = FieldType.Keyword)
    private String lastUpdated;
    
    @Field(type = FieldType.Keyword)
    private String source;
    
    @Field(type = FieldType.Long)
    private Long createdByStoreId;
    
    @Field(type = FieldType.Boolean)
    private Boolean isCustom;
    
    @Field(type = FieldType.Keyword)
    private String approvalStatus;
    
    @Field(type = FieldType.Text)
    private String reasonForRejection;
    
    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd||epoch_millis")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd||epoch_millis")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
    
    // Analytics fields for trending and popularity
    @Field(type = FieldType.Long)
    private Long viewCount = 0L;
    
    @Field(type = FieldType.Long)
    private Long orderCount = 0L;
    
    @Field(type = FieldType.Double)
    private Double averageRating = 0.0;
    
    @Field(type = FieldType.Long)
    private Long reviewCount = 0L;
    
    @Field(type = FieldType.Long)
    private Long searchCount = 0L;
    
    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd||epoch_millis")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime lastOrderedAt;
    
    // Location-based fields
    @Field(type = FieldType.Keyword)
    private List<String> availableLocations;
    
    // Derived fields for search optimization
    @Field(type = FieldType.Double)
    private Double popularityScore; // Calculated from orderCount, viewCount, rating
    
    @Field(type = FieldType.Boolean)
    private Boolean isTrending; // Based on recent activity
    
    @Field(type = FieldType.Boolean)
    private Boolean isNewArrival; // Based on createdAt
    
    @Field(type = FieldType.Boolean)
    private Boolean isBestSeller; // Based on orderCount
    
    @Field(type = FieldType.Boolean)
    private Boolean hasDiscount; // If there are active discounts
    
    // Store information for denormalized search
    @Field(type = FieldType.Keyword)
    private List<Long> availableStoreIds;
    
    @Field(type = FieldType.Keyword)
    private List<String> availableStoreCities;
    
    // Price information from inventory
    @Field(type = FieldType.Double)
    private Double minPrice; // Minimum price across all stores
    
    @Field(type = FieldType.Double)
    private Double maxPrice; // Maximum price across all stores
    
    @Field(type = FieldType.Boolean)
    private Boolean inStock; // Available in any store
}
