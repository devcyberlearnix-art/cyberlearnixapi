package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.elastisearch.InventorySearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventorySearchRepository extends ElasticsearchRepository<InventorySearchDocument, String> {
        List<InventorySearchDocument> findByStoreId(Long storeId);

        List<InventorySearchDocument> findByGtin(String gtin);
        
        List<InventorySearchDocument> findByProductId(Long productId);

        List<InventorySearchDocument> findByStoreIdAndGtin(Long storeId, String gtin);
        
        List<InventorySearchDocument> findByStoreIdAndProductId(Long storeId, Long productId);

        List<InventorySearchDocument> findByStockGreaterThan(int stock);

        // ============= SEARCH METHODS =============

        /**
         * Search inventory by store and radius
         */
        @Query("{\"bool\": {\"must\": [{\"term\": {\"storeId\": ?0}}]}}")
        Page<InventorySearchDocument> findByStoreId(Long storeId, Pageable pageable);

        /**
         * Find products with low stock
         */
        @Query("{\"bool\": {\"must\": [{\"range\": {\"stock\": {\"lte\": ?0}}}]}}")
        List<InventorySearchDocument> findLowStockProducts(int maxStock);

        /**
         * Find available products (stock > 0)
         */
        @Query("{\"bool\": {\"must\": [{\"range\": {\"stock\": {\"gt\": 0}}}]}}")
        List<InventorySearchDocument> findAvailableProducts();

        /**
         * Find products by multiple store IDs
         */
        @Query("{\"bool\": {\"must\": [{\"terms\": {\"storeId\": ?0}}]}}")
        List<InventorySearchDocument> findByStoreIds(List<Long> storeIds);

        /**
         * Find products by store and minimum stock
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"term\": {\"storeId\": ?0}}," +
                        "{\"range\": {\"stock\": {\"gte\": ?1}}}" +
                        "]}}")
        List<InventorySearchDocument> findByStoreIdAndMinStock(Long storeId, int minStock);

        /**
         * Find products by GTIN pattern
         */
        @Query("{\"bool\": {\"should\": [" +
                        "{\"prefix\": {\"gtin\": \"?0\"}}," +
                        "{\"wildcard\": {\"gtin\": \"*?0*\"}}" +
                        "]}}")
        List<InventorySearchDocument> findByGtinContaining(String gtinPart);

        /**
         * Find products by productId pattern
         */
        @Query("{\"bool\": {\"should\": [" +
                        "{\"prefix\": {\"productId\": \"?0\"}}," +
                        "{\"wildcard\": {\"productId\": \"*?0*\"}}" +
                        "]}}")
        List<InventorySearchDocument> findByProductIdContaining(Long productIdPart);

        /**
         * Find products with price range
         */
        @Query("{\"bool\": {\"must\": [{\"range\": {\"price\": {\"gte\": ?0, \"lte\": ?1}}}]}}")
        List<InventorySearchDocument> findByPriceRange(Double minPrice, Double maxPrice);

        /**
         * Find discounted products
         */
        @Query("{\"bool\": {\"must\": [{\"range\": {\"discountPercentage\": {\"gt\": 0}}}]}}")
        List<InventorySearchDocument> findDiscountedProducts();

        /**
         * Find products by store and availability
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"term\": {\"storeId\": ?0}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "]}}")
        List<InventorySearchDocument> findAvailableByStoreId(Long storeId);

        /**
         * Find inventory by store ID and multiple GTINs
         */
        List<InventorySearchDocument> findByStoreIdAndGtinIn(Long storeId, List<String> gtins);

        /**
         * Find inventory by store ID and multiple productIds
         */
        List<InventorySearchDocument> findByStoreIdAndProductIdIn(Long storeId, List<Long> productIds);

        /**
         * Find inventory within a specific distance from coordinates
         * 
         * @param lat        Latitude of the center point
         * @param lon        Longitude of the center point
         * @param distanceKm Distance in kilometers
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "], \"filter\": {" +
                        "\"geo_distance\": {" +
                        "\"distance\": \"?2km\"," +
                        "\"location\": {\"lat\": ?0, \"lon\": ?1}" +
                        "}}}}")
        List<InventorySearchDocument> findInventoryWithinDistance(Double lat, Double lon, Double distanceKm);

        /**
         * Find inventory by product name containing query within distance
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"wildcard\": {\"productName\": \"*?2*\"}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "], \"filter\": {" +
                        "\"geo_distance\": {" +
                        "\"distance\": \"?3km\"," +
                        "\"location\": {\"lat\": ?0, \"lon\": ?1}" +
                        "}}}}")
        List<InventorySearchDocument> findByProductNameAndLocation(Double lat, Double lon, String productName,
                        Double distanceKm);

        /**
         * Find inventory within latitude/longitude bounds (bounding box search)
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "], \"filter\": {" +
                        "\"geo_bounding_box\": {" +
                        "\"location\": {" +
                        "\"top_left\": {\"lat\": ?0, \"lon\": ?1}," +
                        "\"bottom_right\": {\"lat\": ?2, \"lon\": ?3}" +
                        "}}}}")
        List<InventorySearchDocument> findInventoryInBounds(Double topLat, Double leftLon, Double bottomLat,
                        Double rightLon);

        /**
         * Find inventory by category within distance
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"term\": {\"category.keyword\": \"?2\"}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "], \"filter\": {" +
                        "\"geo_distance\": {" +
                        "\"distance\": \"?3km\"," +
                        "\"location\": {\"lat\": ?0, \"lon\": ?1}" +
                        "}}}}")
        List<InventorySearchDocument> findByCategoryAndLocation(Double lat, Double lon, String category,
                        Double distanceKm);

        /**
         * Find inventory by specific productId within distance
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"term\": {\"productId\": ?2}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "], \"filter\": {" +
                        "\"geo_distance\": {" +
                        "\"distance\": \"?3km\"," +
                        "\"location\": {\"lat\": ?0, \"lon\": ?1}" +
                        "}}}}")
        List<InventorySearchDocument> findByProductIdAndLocation(Double lat, Double lon, Long productId,
                        Double distanceKm);

        /**
         * Find inventory by specific GTIN within distance
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"term\": {\"gtin.keyword\": \"?2\"}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "], \"filter\": {" +
                        "\"geo_distance\": {" +
                        "\"distance\": \"?3km\"," +
                        "\"location\": {\"lat\": ?0, \"lon\": ?1}" +
                        "}}}}")
        List<InventorySearchDocument> findByGtinAndLocation(Double lat, Double lon, String gtin,
                        Double distanceKm);

        /**
         * Find inventory by product name containing text (case insensitive)
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"match\": {\"productName\": {\"query\": \"?0\", \"operator\": \"and\", \"fuzziness\": \"AUTO\"}}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "]}}")
        List<InventorySearchDocument> findByProductNameContainingIgnoreCase(String productName);

        /**
         * Search inventory by product name, brand, category and sub-category
         * with fuzzy matching (case insensitive)
         */
        @Query("{\"bool\": {\"should\": [" +
                        "{\"match\": {\"productName\": {\"query\": \"?0\", \"operator\": \"and\", \"boost\": 4, \"fuzziness\": \"AUTO\"}}}" +
                        ",{\"match_phrase_prefix\": {\"productName\": {\"query\": \"?0\", \"boost\": 5}}}" +
                        ",{\"wildcard\": {\"productName.keyword\": \"*?0*\"}}" +
                        ",{\"match\": {\"brandName\": {\"query\": \"?0\", \"operator\": \"and\", \"boost\": 3, \"fuzziness\": \"AUTO\"}}}" +
                        ",{\"match\": {\"category\": {\"query\": \"?0\", \"boost\": 2, \"fuzziness\": \"AUTO\"}}}" +
                        ",{\"match\": {\"subCategory\": {\"query\": \"?0\", \"boost\": 2, \"fuzziness\": \"AUTO\"}}}" +
                        "], \"minimum_should_match\": 1}}")
        List<InventorySearchDocument> searchByProductName(String query);

        /**
         * Find inventory by brand name containing text (case insensitive)
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"match\": {\"brandName\": {\"query\": \"?0\", \"operator\": \"and\", \"fuzziness\": \"AUTO\"}}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "]}}")
        List<InventorySearchDocument> findByBrandNameContainingIgnoreCase(String brandName);

        /**
         * Find inventory by category containing text (case insensitive)
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"match\": {\"category\": {\"query\": \"?0\", \"operator\": \"and\", \"fuzziness\": \"AUTO\"}}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "]}}")
        List<InventorySearchDocument> findByCategoryContainingIgnoreCase(String category);

        /**
         * General search across multiple fields (product name, brand, category)
         */
        @Query("{\"bool\": {\"should\": [" +
                        "{\"match\": {\"productName\": {\"query\": \"?0\", \"boost\": 3, \"fuzziness\": \"AUTO\"}}}," +
                        "{\"match\": {\"brandName\": {\"query\": \"?0\", \"boost\": 2, \"fuzziness\": \"AUTO\"}}}," +
                        "{\"match\": {\"category\": {\"query\": \"?0\", \"boost\": 1, \"fuzziness\": \"AUTO\"}}}," +
                        "{\"match\": {\"description\": {\"query\": \"?0\", \"boost\": 1}}}," +
                        "{\"wildcard\": {\"productName.keyword\": \"*?0*\"}}," +
                        "{\"wildcard\": {\"brandName.keyword\": \"*?0*\"}}" +
                        "], \"minimum_should_match\": 1, \"must\": [" +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "]}}")
        List<InventorySearchDocument> searchInventory(String query);

        /**
         * Search across productName, brandName, storeName, category, description
         * from inventory index only — no join with product/store tables.
         * Boosts: productName=4, brandName=3, storeName=2, category=2, description=1
         */
        @Query("{\"bool\": {\"should\": [" +
                        "{\"match\": {\"productName\": {\"query\": \"?0\", \"boost\": 4, \"fuzziness\": \"AUTO\"}}}," +
                        "{\"match_phrase_prefix\": {\"productName\": {\"query\": \"?0\", \"boost\": 5}}}," +
                        "{\"match\": {\"brandName\": {\"query\": \"?0\", \"boost\": 3, \"fuzziness\": \"AUTO\"}}}," +
                        "{\"match\": {\"storeName\": {\"query\": \"?0\", \"boost\": 2, \"fuzziness\": \"AUTO\"}}}," +
                        "{\"match\": {\"category\": {\"query\": \"?0\", \"boost\": 2, \"fuzziness\": \"AUTO\"}}}," +
                        "{\"match\": {\"subCategory\": {\"query\": \"?0\", \"boost\": 1, \"fuzziness\": \"AUTO\"}}}," +
                        "{\"match\": {\"description\": {\"query\": \"?0\", \"boost\": 1}}}," +
                        "{\"wildcard\": {\"productName.keyword\": \"*?0*\"}}," +
                        "{\"wildcard\": {\"brandName.keyword\": \"*?0*\"}}," +
                        "{\"wildcard\": {\"storeName.keyword\": \"*?0*\"}}" +
                        "], \"minimum_should_match\": 1, \"must\": [" +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "]}}")
        List<InventorySearchDocument> searchInventoryAll(String query);

        /**
         * Find all available inventory (with stock > 0)
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "]}}")
        List<InventorySearchDocument> findAllAvailable();

        /**
         * Find inventory sorted by distance from location
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "], \"filter\": {" +
                        "\"geo_distance\": {" +
                        "\"distance\": \"?2km\"," +
                        "\"location\": {\"lat\": ?0, \"lon\": ?1}" +
                        "}}}, " +
                        "\"sort\": [" +
                        "{\"_geo_distance\": {" +
                        "\"location\": {\"lat\": ?0, \"lon\": ?1}," +
                        "\"order\": \"asc\"," +
                        "\"unit\": \"km\"" +
                        "}}" +
                        "]}")
        List<InventorySearchDocument> findNearestInventory(Double lat, Double lon, Double radiusKm);

        /**
         * Find available inventory (stock > 0) for multiple store IDs.
         * Used when a store-name search is detected to return only that store's inventory.
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"terms\": {\"storeId\": ?0}}," +
                        "{\"range\": {\"stock\": {\"gt\": 0}}}," +
                        "{\"term\": {\"available\": true}}" +
                        "]}}")
        List<InventorySearchDocument> findAvailableByStoreIds(List<Long> storeIds);

        /**
         * Find inventory suggestions by store ID and query across multiple fields
         * Used for autocomplete/suggestions
         */
        @Query("{\"bool\": {\"must\": [" +
                        "{\"term\": {\"storeId\": ?0}}" +
                        "], \"should\": [" +
                        "{\"wildcard\": {\"productName\": \"*?1*\"}}," +
                        "{\"wildcard\": {\"brandName\": \"*?1*\"}}," +
                        "{\"wildcard\": {\"gtin\": \"*?1*\"}}," +
                        "{\"wildcard\": {\"sku\": \"*?1*\"}}" +
                        "], \"minimum_should_match\": 1}}")
        List<InventorySearchDocument> findInventorySuggestionsByStoreAndQuery(Long storeId, String query, Pageable pageable);

        // Add more query methods as needed for your use cases
}
