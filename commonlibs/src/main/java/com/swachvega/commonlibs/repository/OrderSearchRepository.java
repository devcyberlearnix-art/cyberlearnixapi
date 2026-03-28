package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.elastisearch.OrderSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderSearchRepository extends ElasticsearchRepository<OrderSearchDocument, String> {

    List<OrderSearchDocument> findByStoreId(Long storeId);

    Page<OrderSearchDocument> findByStoreId(Long storeId, Pageable pageable);

    List<OrderSearchDocument> findByOrderNumber(String orderNumber);

    List<OrderSearchDocument> findByStatus(String status);

    Page<OrderSearchDocument> findByStoreIdAndStatus(Long storeId, String status, Pageable pageable);

    // Search by order number (partial match)
    @Query("{\"bool\": {\"must\": [" +
            "{\"term\": {\"storeId\": ?0}}," +
            "{\"wildcard\": {\"orderNumber\": \"*?1*\"}}" +
            "]}}")
    Page<OrderSearchDocument> findByStoreIdAndOrderNumberContaining(Long storeId, String orderNumber, Pageable pageable);

    // Full-text search across order number, customer name, status, and item product names
    @Query("{\"bool\": {\"must\": [" +
            "{\"term\": {\"storeId\": ?0}}" +
            "], \"should\": [" +
            "{\"wildcard\": {\"orderNumber\": \"*?1*\"}}," +
            "{\"match\": {\"customerName\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}," +
            "{\"match\": {\"status\": \"?1\"}}," +
            "{\"match\": {\"itemProductNames\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}," +
            "{\"match\": {\"itemBrands\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}" +
            "], \"minimum_should_match\": 1}}")
    Page<OrderSearchDocument> searchOrders(Long storeId, String query, Pageable pageable);

    // Count orders matching a query for a store
    @Query(value = "{\"bool\": {\"must\": [" +
            "{\"term\": {\"storeId\": ?0}}" +
            "], \"should\": [" +
            "{\"wildcard\": {\"orderNumber\": \"*?1*\"}}," +
            "{\"match\": {\"customerName\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}," +
            "{\"match\": {\"status\": \"?1\"}}," +
            "{\"match\": {\"itemProductNames\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}," +
            "{\"match\": {\"itemBrands\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}" +
            "], \"minimum_should_match\": 1}}", count = true)
    long countByStoreIdAndQuery(Long storeId, String query);

    // Autocomplete suggestions for orders
    @Query("{\"bool\": {\"must\": [" +
            "{\"term\": {\"storeId\": ?0}}" +
            "], \"should\": [" +
            "{\"prefix\": {\"orderNumber\": \"?1\"}}," +
            "{\"wildcard\": {\"orderNumber\": \"*?1*\"}}," +
            "{\"match_phrase_prefix\": {\"customerName\": \"?1\"}}," +
            "{\"match\": {\"customerName\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}," +
            "{\"match_phrase_prefix\": {\"itemProductNames\": \"?1\"}}," +
            "{\"match\": {\"itemProductNames\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}," +
            "{\"match\": {\"itemBrands\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}" +
            "], \"minimum_should_match\": 1}}")
    List<OrderSearchDocument> findOrderSuggestions(Long storeId, String query, Pageable pageable);

    // Find orders by customer name
    @Query("{\"bool\": {\"must\": [" +
            "{\"term\": {\"storeId\": ?0}}," +
            "{\"match\": {\"customerName\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}" +
            "]}}")
    Page<OrderSearchDocument> findByStoreIdAndCustomerName(Long storeId, String customerName, Pageable pageable);

    // Find orders containing a specific product
    @Query("{\"bool\": {\"must\": [" +
            "{\"term\": {\"storeId\": ?0}}," +
            "{\"match\": {\"itemProductNames\": {\"query\": \"?1\", \"fuzziness\": \"AUTO\"}}}" +
            "]}}")
    Page<OrderSearchDocument> findByStoreIdAndProductName(Long storeId, String productName, Pageable pageable);
}
