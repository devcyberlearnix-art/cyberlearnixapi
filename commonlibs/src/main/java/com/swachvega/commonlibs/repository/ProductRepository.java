package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

        interface ProductSnapshot {
                Long getProductId();

                String getProductName();

                String getDescription();

                String getUnitOfMeasure();

                String getBrandName();

                String getFrontImageUrl();

                BigDecimal getGrossWeightG();

                                BigDecimal getMrp();

                                BigDecimal getGstRate();

                                BigDecimal getIgstRate();

                                BigDecimal getCgstRate();

                                BigDecimal getSgstRate();
        }

                @Query("SELECT p.productId AS productId, p.productName AS productName, p.description AS description, " +
                                                "p.unitOfMeasure AS unitOfMeasure, p.brandName AS brandName, p.frontImageUrl AS frontImageUrl, " +
                                                "p.grossWeightG AS grossWeightG, p.mrp AS mrp, " +
                                                "p.gstRate AS gstRate, p.igstRate AS igstRate, p.cgstRate AS cgstRate, p.sgstRate AS sgstRate " +
                                                "FROM Product p WHERE p.productId = :productId")
        Optional<ProductSnapshot> findSnapshotById(@Param("productId") Long productId);

        /**
         * Batch fetch product snapshots by product IDs to prevent N+1 queries
         */
        @Query("SELECT p.productId AS productId, p.productName AS productName, p.description AS description, " +
                        "p.unitOfMeasure AS unitOfMeasure, p.brandName AS brandName, p.frontImageUrl AS frontImageUrl, " +
                        "p.grossWeightG AS grossWeightG, p.mrp AS mrp, " +
                        "p.gstRate AS gstRate, p.igstRate AS igstRate, p.cgstRate AS cgstRate, p.sgstRate AS sgstRate " +
                        "FROM Product p WHERE p.productId IN :productIds")
        List<ProductSnapshot> findSnapshotsByIds(@Param("productIds") List<Long> productIds);

    /**
     * Find product by GTIN
     */
    Optional<Product> findByGtin(String gtin);

    /**
     * Find products by category
     */
    @Query("SELECT p FROM Product p LEFT JOIN p.primaryCategory pc " +
            "WHERE LOWER(pc.name) = LOWER(:category) OR LOWER(pc.displayName) = LOWER(:category) OR LOWER(pc.slug) = LOWER(:category)")
    List<Product> findByCategory(@Param("category") String category);

    @Query("SELECT p FROM Product p LEFT JOIN p.primaryCategory pc " +
            "WHERE pc.id IS NOT NULL AND (" +
            "LOWER(pc.name) LIKE LOWER(CONCAT('%', :category, '%')) OR " +
            "LOWER(pc.displayName) LIKE LOWER(CONCAT('%', :category, '%')) OR " +
            "LOWER(pc.slug) LIKE LOWER(CONCAT('%', :category, '%')))" )
    List<Product> findByCategoryContainingIgnoreCase(@Param("category") String category);

    /**
     * Find products by brand
     */
    List<Product> findByBrandName(String brandName);

    @Query("SELECT p FROM Product p WHERE LOWER(p.brandName) LIKE LOWER(CONCAT('%', :brandName, '%'))")
    List<Product> findByBrandNameContainingIgnoreCase(@Param("brandName") String brandName);

    /**
     * Find products by name containing (case insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByProductNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT p FROM Product p LEFT JOIN p.subCategoryEntity sc " +
            "WHERE sc.id IS NOT NULL AND (" +
            "LOWER(sc.name) LIKE LOWER(CONCAT('%', :subCategory, '%')) OR " +
            "LOWER(sc.displayName) LIKE LOWER(CONCAT('%', :subCategory, '%')) OR " +
            "LOWER(sc.slug) LIKE LOWER(CONCAT('%', :subCategory, '%')))" )
    List<Product> findBySubCategoryContainingIgnoreCase(@Param("subCategory") String subCategory);

    /**
     * Find products by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Check if product exists by GTIN
     */
    boolean existsByGtin(String gtin);

    /**
     * Find products by multiple GTINs
     */
    List<Product> findByGtinIn(List<String> gtins);

    /**
     * Find products by category and subcategory
     */
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.primaryCategory pc " +
            "LEFT JOIN p.subCategoryEntity sc " +
            "WHERE (LOWER(pc.name) = LOWER(:category) OR LOWER(pc.displayName) = LOWER(:category) OR LOWER(pc.slug) = LOWER(:category)) " +
            "AND (LOWER(sc.name) = LOWER(:subCategory) OR LOWER(sc.displayName) = LOWER(:subCategory) OR LOWER(sc.slug) = LOWER(:subCategory))")
    List<Product> findByCategoryAndSubCategory(@Param("category") String category, @Param("subCategory") String subCategory);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.primaryCategory pc " +
            "LEFT JOIN p.subCategoryEntity sc " +
            "WHERE (" +
            "LOWER(pc.name) LIKE LOWER(CONCAT('%', :category, '%')) OR " +
            "LOWER(pc.displayName) LIKE LOWER(CONCAT('%', :category, '%')) OR " +
            "LOWER(pc.slug) LIKE LOWER(CONCAT('%', :category, '%'))" +
            ") AND (" +
            "LOWER(sc.name) LIKE LOWER(CONCAT('%', :subCategory, '%')) OR " +
            "LOWER(sc.displayName) LIKE LOWER(CONCAT('%', :subCategory, '%')) OR " +
            "LOWER(sc.slug) LIKE LOWER(CONCAT('%', :subCategory, '%'))" +
            ")")
    List<Product> findByCategoryAndSubCategoryContainingIgnoreCase(@Param("category") String category, @Param("subCategory") String subCategory);
}
