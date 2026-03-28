package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.elastisearch.ProductSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDocument, String> {

       // Basic search queries
       ProductSearchDocument findByGtin(String gtin);
       
       List<ProductSearchDocument> findByProductNameContainingIgnoreCase(String productName);

       Page<ProductSearchDocument> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

       List<ProductSearchDocument> findByCategory(String category);

       Page<ProductSearchDocument> findByCategory(String category, Pageable pageable);

       List<ProductSearchDocument> findByBrandName(String brandName);

       Page<ProductSearchDocument> findByBrandName(String brandName, Pageable pageable);

       List<ProductSearchDocument> findByIsCustom(Boolean isCustom);

       List<ProductSearchDocument> findByCreatedByStoreId(Long storeId);

       // Location-based queries
       @Query("{\"bool\": {\"must\": [{\"terms\": {\"availableLocations\": [\"?0\"]}}]}}")
       List<ProductSearchDocument> findByAvailableLocation(String location);

       @Query("{\"bool\": {\"must\": [{\"terms\": {\"availableLocations\": [\"?0\"]}}, {\"term\": {\"category\": \"?1\"}}]}}")
       List<ProductSearchDocument> findByAvailableLocationAndCategory(String location, String category);

       @Query("{\"bool\": {\"must\": [{\"terms\": {\"availableLocations\": [\"?0\"]}}, {\"term\": {\"category\": \"?1\"}}]}}")
       Page<ProductSearchDocument> findByAvailableLocationAndCategory(String location, String category,
                     Pageable pageable);

       // Trending and popular products
       @Query("{\"bool\": {\"must\": [{\"term\": {\"isTrending\": true}}]}}")
       List<ProductSearchDocument> findTrendingProducts(Pageable pageable);

       @Query("{\"bool\": {\"must\": [{\"term\": {\"isTrending\": true}}, {\"terms\": {\"availableLocations\": [\"?0\"]}}]}}")
       List<ProductSearchDocument> findTrendingProductsByLocation(String location, Pageable pageable);

       @Query("{\"bool\": {\"must\": [{\"term\": {\"isBestSeller\": true}}]}}")
       List<ProductSearchDocument> findBestSellers(Pageable pageable);

       @Query("{\"bool\": {\"must\": [{\"term\": {\"isBestSeller\": true}}, {\"terms\": {\"availableLocations\": [\"?0\"]}}]}}")
       List<ProductSearchDocument> findBestSellersByLocation(String location, Pageable pageable);

       @Query("{\"bool\": {\"must\": [{\"term\": {\"isNewArrival\": true}}]}}")
       List<ProductSearchDocument> findNewArrivals(Pageable pageable);

       @Query("{\"bool\": {\"must\": [{\"term\": {\"isNewArrival\": true}}, {\"terms\": {\"availableLocations\": [\"?0\"]}}]}}")
       List<ProductSearchDocument> findNewArrivalsByLocation(String location, Pageable pageable);

       @Query("{\"bool\": {\"must\": [{\"term\": {\"hasDiscount\": true}}]}}")
       List<ProductSearchDocument> findProductsWithDiscounts(Pageable pageable);

       @Query("{\"bool\": {\"must\": [{\"term\": {\"hasDiscount\": true}}, {\"terms\": {\"availableLocations\": [\"?0\"]}}]}}")
       List<ProductSearchDocument> findProductsWithDiscountsByLocation(String location, Pageable pageable);

       // Advanced search with multiple filters
       @Query("{\"bool\": {\"must\": [" +
                     "{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"productName^3\", \"brandName^2\", \"category\", \"description\"]}},"
                     +
                     "{\"terms\": {\"availableLocations\": [\"?1\"]}}" +
                     "]}}")
       Page<ProductSearchDocument> searchByQueryAndLocation(String query, String location, Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"productName^3\", \"brandName^2\", \"category\", \"description\"]}},"
                     +
                     "{\"term\": {\"category\": \"?1\"}}," +
                     "{\"terms\": {\"availableLocations\": [\"?2\"]}}" +
                     "]}}")
       Page<ProductSearchDocument> searchByQueryCategoryAndLocation(String query, String category, String location,
                     Pageable pageable);

       // Price range queries
       @Query("{\"bool\": {\"must\": [{\"range\": {\"mrp\": {\"gte\": ?0, \"lte\": ?1}}}]}}")
       List<ProductSearchDocument> findByPriceRange(Double minPrice, Double maxPrice);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"range\": {\"mrp\": {\"gte\": ?0, \"lte\": ?1}}}," +
                     "{\"terms\": {\"availableLocations\": [\"?2\"]}}" +
                     "]}}")
       List<ProductSearchDocument> findByPriceRangeAndLocation(Double minPrice, Double maxPrice, String location);

       // Autocomplete and suggestions
       @Query("{\"bool\": {\"should\": [" +
                     "{\"match_phrase_prefix\": {\"productName\": \"?0\"}}," +
                     "{\"match_phrase_prefix\": {\"brandName\": \"?0\"}}" +
                     "]}}")
       List<ProductSearchDocument> findProductNamesForAutocomplete(String prefix, Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"match_phrase_prefix\": {\"productName\": \"?0\"}}," +
                     "{\"terms\": {\"availableLocations\": [\"?1\"]}}" +
                     "]}}")
       List<ProductSearchDocument> findProductNamesForAutocompleteByLocation(String prefix, String location,
                     Pageable pageable);

       // Analytics queries
       @Query("{\"aggs\": {\"categories\": {\"terms\": {\"field\": \"category\"}}}}")
       List<ProductSearchDocument> findDistinctCategories();

       @Query("{\"bool\": {\"must\": [{\"terms\": {\"availableLocations\": [\"?0\"]}}]}, " +
                     "\"aggs\": {\"categories\": {\"terms\": {\"field\": \"category\"}}}}")
       List<ProductSearchDocument> findDistinctCategoriesByLocation(String location);

       @Query("{\"aggs\": {\"brands\": {\"terms\": {\"field\": \"brandName\"}}}}")
       List<ProductSearchDocument> findDistinctBrands();

       @Query("{\"bool\": {\"must\": [{\"terms\": {\"availableLocations\": [\"?0\"]}}]}, " +
                     "\"aggs\": {\"brands\": {\"terms\": {\"field\": \"brandName\"}}}}")
       List<ProductSearchDocument> findDistinctBrandsByLocation(String location);

       // Stock and availability
       @Query("{\"bool\": {\"must\": [{\"term\": {\"inStock\": true}}]}}")
       List<ProductSearchDocument> findInStockProducts();

       @Query("{\"bool\": {\"must\": [{\"term\": {\"inStock\": true}}, {\"terms\": {\"availableLocations\": [\"?0\"]}}]}}")
       List<ProductSearchDocument> findInStockProductsByLocation(String location);

       // Store-specific queries
       @Query("{\"bool\": {\"must\": [{\"terms\": {\"availableStoreIds\": [?0]}}]}}")
       List<ProductSearchDocument> findByAvailableStoreId(Long storeId);

       @Query("{\"bool\": {\"must\": [{\"terms\": {\"availableStoreCities\": [\"?0\"]}}]}}")
       List<ProductSearchDocument> findByAvailableStoreCity(String city);

       // ============= SUGGESTION METHODS =============

       /**
        * Get product name suggestions for autocomplete
        * Searches in: productName, gtin, sku
        */
       @Query("{" +
                     "  \"bool\": {" +
                     "    \"should\": [" +
                     "      {\"match_phrase_prefix\": {\"productName\": \"?0\"}}," +
                     "      {\"prefix\": {\"productName.keyword\": \"?0\"}}," +
                     "      {\"prefix\": {\"gtin\": \"?0\"}}," +
                     "      {\"prefix\": {\"sku.keyword\": \"?0\"}}," +
                     "      {\"wildcard\": {\"gtin\": \"*?0*\"}}," +
                     "      {\"wildcard\": {\"sku\": \"*?0*\"}}" +
                     "    ]" +
                     "  }" +
                     "}")
       List<ProductSearchDocument> findProductNameSuggestions(String query, Pageable pageable);

       /**
        * Get category suggestions for autocomplete
        */
       @Query("{" +
                     "  \"bool\": {" +
                     "    \"should\": [" +
                     "      {\"prefix\": {\"category.keyword\": \"?0\"}}," +
                     "      {\"wildcard\": {\"category\": \"*?0*\"}}" +
                     "    ]" +
                     "  }" +
                     "}")
       List<ProductSearchDocument> findCategorySuggestions(String query, Pageable pageable);

       /**
        * Get brand suggestions for autocomplete
        */
       @Query("{" +
                     "  \"bool\": {" +
                     "    \"should\": [" +
                     "      {\"prefix\": {\"brandName.keyword\": \"?0\"}}," +
                     "      {\"wildcard\": {\"brandName\": \"*?0*\"}}" +
                     "    ]" +
                     "  }" +
                     "}")
       List<ProductSearchDocument> findBrandSuggestions(String query, Pageable pageable);

       /**
        * Get distinct categories as strings
        */
       default List<String> findDistinctCategoriesAsStrings() {
              return findDistinctCategories().stream()
                            .map(ProductSearchDocument::getPrimaryCategory)
                            .distinct()
                            .collect(java.util.stream.Collectors.toList());
       }

       /**
        * Convert product name suggestions to strings
        */
       default List<String> findProductNameSuggestions(String query, int limit) {
              return findProductNameSuggestions(query, PageRequest.of(0, limit)).stream()
                            .map(ProductSearchDocument::getProductName)
                            .distinct()
                            .collect(java.util.stream.Collectors.toList());
       }

       /**
        * Convert category suggestions to strings
        */
       default List<String> findCategorySuggestions(String query, int limit) {
              return findCategorySuggestions(query, PageRequest.of(0, limit)).stream()
                            .map(ProductSearchDocument::getPrimaryCategory)
                            .distinct()
                            .collect(java.util.stream.Collectors.toList());
       }

       /**
        * Convert brand suggestions to strings
        */
       default List<String> findBrandSuggestions(String query, int limit) {
              return findBrandSuggestions(query, PageRequest.of(0, limit)).stream()
                            .map(ProductSearchDocument::getBrandName)
                            .distinct()
                            .collect(java.util.stream.Collectors.toList());
       }
}
