package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.elastisearch.StoreSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreSearchRepository extends ElasticsearchRepository<StoreSearchDocument, Long> {

       // Basic search queries
       List<StoreSearchDocument> findByStoreNameContainingIgnoreCase(String storeName);

       Page<StoreSearchDocument> findByStoreNameContainingIgnoreCase(String storeName, Pageable pageable);

       List<StoreSearchDocument> findByCity(String city);

       Page<StoreSearchDocument> findByCity(String city, Pageable pageable);

       List<StoreSearchDocument> findByIsTrending(Boolean isTrending);

       List<StoreSearchDocument> findByIsOpen(Boolean isOpen);

       List<StoreSearchDocument> findByCategoriesContaining(String category);

       Page<StoreSearchDocument> findByCategoriesContaining(String category, Pageable pageable);

       // Location-based geo queries
       @Query("{\"bool\": {\"must\": [{\"geo_distance\": {\"distance\": \"?2km\", \"location\": {\"lat\": ?0, \"lon\": ?1}}}]}}")
       List<StoreSearchDocument> findNearbyStores(Double latitude, Double longitude, Double radiusKm);

       @Query("{\"bool\": {\"must\": [{\"geo_distance\": {\"distance\": \"?2km\", \"location\": {\"lat\": ?0, \"lon\": ?1}}}]}}")
       Page<StoreSearchDocument> findNearbyStores(Double latitude, Double longitude, Double radiusKm,
                     Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"geo_distance\": {\"distance\": \"?2km\", \"location\": {\"lat\": ?0, \"lon\": ?1}}}," +
                     "{\"term\": {\"isOpen\": true}}" +
                     "]}}")
       List<StoreSearchDocument> findNearbyOpenStores(Double latitude, Double longitude, Double radiusKm);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"geo_distance\": {\"distance\": \"?3km\", \"location\": {\"lat\": ?0, \"lon\": ?1}}}," +
                     "{\"terms\": {\"categories\": [\"?2\"]}}" +
                     "]}}")
       List<StoreSearchDocument> findNearbyStoresByCategory(Double latitude, Double longitude, String category,
                     Double radiusKm);

       // Trending and popular stores
       @Query("{\"bool\": {\"must\": [{\"term\": {\"isTrending\": true}}]}}")
       List<StoreSearchDocument> findTrendingStores(Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"term\": {\"isTrending\": true}}," +
                     "{\"term\": {\"city\": \"?0\"}}" +
                     "]}}")
       List<StoreSearchDocument> findTrendingStoresByCity(String city, Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"term\": {\"isTrending\": true}}," +
                     "{\"geo_distance\": {\"distance\": \"?2km\", \"location\": {\"lat\": ?0, \"lon\": ?1}}}" +
                     "]}}")
       List<StoreSearchDocument> findTrendingStoresByLocation(Double latitude, Double longitude, Double radiusKm,
                     Pageable pageable);

       // High-rated stores
       @Query("{\"bool\": {\"must\": [{\"range\": {\"rating\": {\"gte\": ?0}}}]}}")
       List<StoreSearchDocument> findHighRatedStores(Double minRating, Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"range\": {\"rating\": {\"gte\": ?2}}}," +
                     "{\"geo_distance\": {\"distance\": \"?3km\", \"location\": {\"lat\": ?0, \"lon\": ?1}}}" +
                     "]}}")
       List<StoreSearchDocument> findHighRatedNearbyStores(Double latitude, Double longitude, Double minRating,
                     Double radiusKm, Pageable pageable);

       // Search with filters
       @Query("{\"bool\": {\"must\": [" +
                     "{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"storeName^3\", \"description\", \"categories\"]}},"
                     +
                     "{\"term\": {\"city\": \"?1\"}}" +
                     "]}}")
       Page<StoreSearchDocument> searchByQueryAndCity(String query, String city, Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"storeName^3\", \"description\", \"categories\"]}},"
                     +
                     "{\"geo_distance\": {\"distance\": \"?3km\", \"location\": {\"lat\": ?1, \"lon\": ?2}}}" +
                     "]}}")
       Page<StoreSearchDocument> searchByQueryAndLocation(String query, Double latitude, Double longitude,
                     Double radiusKm, Pageable pageable);

       // Store type queries
       List<StoreSearchDocument> findByStoreType(String storeType);

       Page<StoreSearchDocument> findByStoreType(String storeType, Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"term\": {\"storeType\": \"?0\"}}," +
                     "{\"term\": {\"city\": \"?1\"}}" +
                     "]}}")
       List<StoreSearchDocument> findByStoreTypeAndCity(String storeType, String city);

       // Premium and delivery features
       @Query("{\"bool\": {\"must\": [{\"term\": {\"premiumStatus\": true}}]}}")
       List<StoreSearchDocument> findPremiumStores(Pageable pageable);

       @Query("{\"bool\": {\"must\": [{\"term\": {\"homeDelivery\": true}}]}}")
       List<StoreSearchDocument> findStoresWithDelivery();

       @Query("{\"bool\": {\"must\": [{\"term\": {\"acceptsOnlinePayment\": true}}]}}")
       List<StoreSearchDocument> findStoresWithOnlinePayment();

       // Hours and availability
       @Query("{\"bool\": {\"must\": [{\"term\": {\"offers24x7\": true}}]}}")
       List<StoreSearchDocument> find24x7Stores();

       @Query("{\"bool\": {\"must\": [" +
                     "{\"term\": {\"offers24x7\": true}}," +
                     "{\"geo_distance\": {\"distance\": \"?2km\", \"location\": {\"lat\": ?0, \"lon\": ?1}}}" +
                     "]}}")
       List<StoreSearchDocument> find24x7NearbyStores(Double latitude, Double longitude, Double radiusKm);
       /**
        * Strict phrase match on storeName — all words in the query must appear
        * consecutively in the store name (e.g. "BN Reddy" matches "BN Reddy store"
        * but NOT "CyberLearnix Fresh"). Used for store-name search detection.
        */
       @Query("{\"bool\": {\"must\": [{\"match_phrase\": {\"storeName\": \"?0\"}}]}}")
       List<StoreSearchDocument> findByStoreNamePhrase(String storeName);
       // Autocomplete
       @Query("{\"bool\": {\"should\": [" +
                     "{\"match_phrase_prefix\": {\"storeName\": \"?0\"}}," +
                     "{\"match_phrase_prefix\": {\"categories\": \"?0\"}}" +
                     "]}}")
       List<StoreSearchDocument> findStoreNamesForAutocomplete(String prefix, Pageable pageable);

       @Query("{\"bool\": {\"must\": [" +
                     "{\"match_phrase_prefix\": {\"storeName\": \"?0\"}}," +
                     "{\"term\": {\"city\": \"?1\"}}" +
                     "]}}")
       List<StoreSearchDocument> findStoreNamesForAutocompleteByCity(String prefix, String city, Pageable pageable);

       // Analytics
       @Query("{\"aggs\": {\"cities\": {\"terms\": {\"field\": \"city\"}}}}")
       List<StoreSearchDocument> findDistinctCities();

       @Query("{\"aggs\": {\"categories\": {\"terms\": {\"field\": \"categories\"}}}}")
       List<StoreSearchDocument> findDistinctCategories();

       @Query("{\"aggs\": {\"storeTypes\": {\"terms\": {\"field\": \"storeType\"}}}}")
       List<StoreSearchDocument> findDistinctStoreTypes();

       // ============= SUGGESTION HELPER METHODS =============

       /**
        * Get store name suggestions as strings
        */
       default List<String> findStoreNameSuggestions(String query, int limit) {
              return findStoreNamesForAutocomplete(query, PageRequest.of(0, limit)).stream()
                            .map(StoreSearchDocument::getStoreName)
                            .distinct()
                            .collect(java.util.stream.Collectors.toList());
       }

       /**
        * Get distinct cities as strings
        */
       default List<String> findDistinctCitiesAsStrings() {
              return findDistinctCities().stream()
                            .map(StoreSearchDocument::getCity)
                            .distinct()
                            .collect(java.util.stream.Collectors.toList());
       }

       /**
        * Get distinct store types as strings
        */
       default List<String> findDistinctStoreTypesAsStrings() {
              return findDistinctStoreTypes().stream()
                            .map(StoreSearchDocument::getStoreType)
                            .distinct()
                            .collect(java.util.stream.Collectors.toList());
       }
}
