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
 * DTO for mv_zipcode_trending_categories materialized view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mv_zipcode_trending_categories")
public class ZipcodeTrendingCategory {
    @Id
    @Column(name = "category")
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "category_display_name")
    private String categoryDisplayName;

    @Column(name = "sub_category_display_name")
    private String subCategoryDisplayName;

    @Column(name = "category_image_url")
    private String categoryImageUrl;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "city")
    private String city;

    @Column(name = "order_count_7d")
    private Long orderCount7d;

    @Column(name = "unique_buyers_7d")
    private Long uniqueBuyers7d;

    @Column(name = "total_items_sold_7d")
    private Long totalItemsSold7d;

    @Column(name = "revenue_7d")
    private BigDecimal revenue7d;

    @Column(name = "unique_products")
    private Long uniqueProducts;

    @Column(name = "rank_in_zipcode")
    private Integer rankInZipcode;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
