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
 * DTO for mv_zipcode_frequent_products materialized view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mv_zipcode_frequent_products")
public class ZipcodeFrequentProduct {
    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "gtin")
    private String gtin;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "category")
    private String category;

    @Column(name = "category_display_name")
    private String categoryDisplayName;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "city")
    private String city;

    @Column(name = "total_buyers_30d")
    private Long totalBuyers30d;

    @Column(name = "repeat_buyers_30d")
    private Long repeatBuyers30d;

    @Column(name = "repeat_rate_pct")
    private BigDecimal repeatRatePct;

    @Column(name = "total_orders_30d")
    private Long totalOrders30d;

    @Column(name = "total_quantity_30d")
    private Long totalQuantity30d;

    @Column(name = "avg_price")
    private BigDecimal avgPrice;

    @Column(name = "rank_in_zipcode")
    private Integer rankInZipcode;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
