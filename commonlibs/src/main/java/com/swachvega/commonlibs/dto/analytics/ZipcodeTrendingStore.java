package com.cyberlearnix.commonlibs.dto.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for mv_zipcode_trending_stores materialized view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mv_zipcode_trending_stores")
public class ZipcodeTrendingStore {
    @Id
    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_type")
    private String storeType;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "city")
    private String city;

    @Column(name = "locality")
    private String locality;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "premium_status")
    private Boolean premiumStatus;

    @Column(name = "is_trending")
    private Boolean isTrending;

    @Column(name = "store_rating")
    private BigDecimal storeRating;

    @Column(name = "order_count_7d")
    private Long orderCount7d;

    @Column(name = "unique_customers_7d")
    private Long uniqueCustomers7d;

    @Column(name = "revenue_7d")
    private BigDecimal revenue7d;

    @Column(name = "avg_order_value")
    private BigDecimal avgOrderValue;

    @Column(name = "rank_in_zipcode")
    private Integer rankInZipcode;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
