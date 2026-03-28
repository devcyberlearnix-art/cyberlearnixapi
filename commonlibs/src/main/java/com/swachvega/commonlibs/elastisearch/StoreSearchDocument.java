package com.cyberlearnix.commonlibs.elastisearch;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.cyberlearnix.commonlibs.util.FlexibleLocalDateTimeDeserializer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Document(indexName = "stores")
public class StoreSearchDocument {

    @Id
    private Long storeId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String storeName;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String state;

    @Field(type = FieldType.Keyword)
    private String zipCode;

    @Field(type = FieldType.Keyword)
    private String area;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Keyword)
    private String website;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @GeoPointField
    private String location; // Format: "lat,lon"

    @Field(type = FieldType.Double)
    private Double latitude;

    @Field(type = FieldType.Double)
    private Double longitude;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Boolean)
    private Boolean isTrending;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Integer)
    private Integer ratingCount;

    @Field(type = FieldType.Integer)
    private Integer orderCount;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss||uuuu-MM-dd'T'HH:mm||uuuu-MM-dd||epoch_millis")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss||uuuu-MM-dd'T'HH:mm||uuuu-MM-dd||epoch_millis")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Keyword)
    private LocalTime openTime;

    @Field(type = FieldType.Keyword)
    private LocalTime closeTime;

    @Field(type = FieldType.Keyword)
    private List<String> categories;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Double)
    private Double storeScore;

    @Field(type = FieldType.Boolean)
    private Boolean premiumStatus;

    @Field(type = FieldType.Boolean)
    private Boolean isOpen;

    @Field(type = FieldType.Integer)
    private Integer deliveryEtaMinutes;

    @Field(type = FieldType.Keyword)
    private String gstNumber;

    @Field(type = FieldType.Boolean)
    private Boolean gstRegistered;

    @Field(type = FieldType.Keyword)
    private String licenseNumber;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss||uuuu-MM-dd'T'HH:mm||uuuu-MM-dd||epoch_millis")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime licenseValidTill;

    // Enhanced analytics fields
    @Field(type = FieldType.Long)
    private Long viewCount = 0L;

    @Field(type = FieldType.Long)
    private Long searchCount = 0L;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss||uuuu-MM-dd'T'HH:mm||uuuu-MM-dd||epoch_millis")
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime lastOrderAt;

    // Operating hours for each day
    @Field(type = FieldType.Keyword)
    private String mondayHours;

    @Field(type = FieldType.Keyword)
    private String tuesdayHours;

    @Field(type = FieldType.Keyword)
    private String wednesdayHours;

    @Field(type = FieldType.Keyword)
    private String thursdayHours;

    @Field(type = FieldType.Keyword)
    private String fridayHours;

    @Field(type = FieldType.Keyword)
    private String saturdayHours;

    @Field(type = FieldType.Keyword)
    private String sundayHours;

    // Store metadata
    @Field(type = FieldType.Keyword)
    private String storeType;

    @Field(type = FieldType.Double)
    private Double minOrderAmount;

    @Field(type = FieldType.Double)
    private Double deliveryFee;

    @Field(type = FieldType.Double)
    private Double freeDeliveryThreshold;

    @Field(type = FieldType.Double)
    private Double serviceRadiusKm;

    // Derived fields for search optimization
    @Field(type = FieldType.Double)
    private Double popularityScore; // Based on rating, orders, views

    @Field(type = FieldType.Boolean)
    private Boolean hasDiscounts;

    @Field(type = FieldType.Boolean)
    private Boolean offers24x7;

    @Field(type = FieldType.Integer)
    private Integer totalProducts; // Count of products available

    @Field(type = FieldType.Boolean)
    private Boolean acceptsOnlinePayment;

    @Field(type = FieldType.Boolean)
    private Boolean homeDelivery;
}
