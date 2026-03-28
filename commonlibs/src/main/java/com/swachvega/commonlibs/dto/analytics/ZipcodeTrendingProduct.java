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
 * DTO for mv_zipcode_trending_products materialized view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mv_zipcode_trending_products")
public class ZipcodeTrendingProduct {
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

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "category_display_name")
    private String categoryDisplayName;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "city")
    private String city;

    @Column(name = "order_count_7d")
    private Long orderCount7d;

    @Column(name = "total_quantity_sold_7d")
    private Long totalQuantitySold7d;

    @Column(name = "unique_buyers_7d")
    private Long uniqueBuyers7d;

    @Column(name = "revenue_7d")
    private BigDecimal revenue7d;

    @Column(name = "avg_price")
    private BigDecimal avgPrice;

    @Column(name = "rank_in_zipcode")
    private Integer rankInZipcode;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
