package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive Inventory Response DTO
 * Aligned with SearchResponseV2.InventoryResultItem structure
 * for consistent API responses across services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class InventoryResponseDTO {
    
    // Basic inventory info
    private String inventoryId;
    private Long productId;
    private String gtin;
    private String productName;
    private String brandName;
    private CategoryInfo category;
    private String description;
    private String imageUrl;
    private String unit; // "kg", "g", "l", "ml", "pieces"
    private Double quantity; // Net quantity (e.g., 1.0 for 1kg)
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private Long subCategoryId;
        private String subCategoryName;
    }
    
    // Store location at top level for easy access
    private Double latitude;
    private Double longitude;
    
    // Store and location info
    private StoreInfo store;
    private DistanceInfo distance;
    
    // Pricing info
    private PricingInfo pricing;
    
    // Availability info
    private AvailabilityInfo availability;
    
    // Quick actions
    private ProductActions actions;
    
    // Additional inventory-specific fields grouped as metadata
    private ProductMetadata metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductMetadata {
        private String batchNumber;
        private LocalDate expiryDate;
        private LocalDateTime lastUpdated;
        private Integer lowStockThreshold;
        private String hsnCode;
        private String supplierInfo;
        private String warehouseLocation;
    }
    
    // GST rate fields
    private Double gstRate;
    private Double igstRate;
    private Double cgstRate;
    private Double sgstRate;
    

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreInfo {
        private Long storeId;
        private String storeName;
        private String merchantName;
        private String merchantRegistrationId;
        private String storeType; // "SUPERMARKET", "EXPRESS", "PREMIUM", etc.
        private String address;
        private Double latitude;
        private Double longitude;
        private String phone;
        private Double rating;
        private Integer ratingCount;
        private String operatingHours;
        private Boolean isOpen;
        private Boolean deliveryAvailable;
        private BigDecimal deliveryFee;
        private BigDecimal minOrderAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistanceInfo {
        private Double distanceKm;
        private String distanceText; // "2.5 km away"
        /**
         * Total ETA shown to the user (prep + travel + buffer), e.g. "30-45 mins".
         *
         * Historically this field was used as merchant preparation time in some clients.
         * If you need the merchant-provided prep time, use preparationTimeMinutes / preparationTimeText.
         */
        private String estimatedDeliveryTime;
        /** Merchant-provided preparation time in minutes. */
        private Integer preparationTimeMinutes;
        /** Merchant-provided preparation time text (e.g., "20 mins"). */
        private String preparationTimeText;
        private Boolean isNearestStore;
        private Integer storeRank; // 1st nearest, 2nd nearest, etc.
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingInfo {
        private BigDecimal mrp; // Maximum Retail Price - should be highest
        private BigDecimal sellingPrice; // Actual selling price
        private BigDecimal discount; // Discount amount in currency
        private Double discountPercentage; // Discount as percentage
        private String discountText; // "Save ₹50" or "20% off"
        private String pricePerUnit; // "₹150/kg"
        private String currency; // "INR", "USD", etc.
        private BigDecimal wholesalePrice;
        private Boolean onSale;
        private LocalDateTime saleEndTime;
        private Boolean isLowestPrice;
        private String priceComparison; // "₹20 cheaper than other stores"
        // Tax information
        private TaxInfo tax;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxInfo {
        private Double gstRate;
        private Double cgst;
        private Double sgst;
        private Double igst;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityInfo {
        private Boolean available;
        private StockInfo stock;
        private DeliveryInfo delivery;
        private RestockInfo restock;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockInfo {
        private Integer total;
        private Integer available;
        private Integer reserved;
        private Integer lowStockThreshold;
        private String status; // "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"
        private String statusText; // "Only 3 left", "In stock", "Out of stock"
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfo {
        private Boolean fastDelivery; // Can be delivered in <2 hours
        private Boolean expressDelivery; // Can be delivered in <1 hour
        private List<String> availableSlots; // ["2-4 PM", "4-6 PM"]
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestockInfo {
        private LocalDateTime nextRestockDate;
        private String restockFrequency; // "daily", "weekly", "monthly"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductActions {
        private CartActions cart;
        private WishlistActions wishlist;
        private OrderingOptions ordering;
        private List<String> available; // ["add_to_cart", "add_to_favorites", "quick_view"]
        private ActionUrls urls;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartActions {
        private Boolean canAdd;
        private Integer minQuantity;
        private Integer maxQuantity;
        private Integer currentQuantity;
        private String buttonText; // "Add to Cart", "Update Quantity"
        private Boolean showQuantitySelector;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WishlistActions {
        private Boolean isFavorite;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderingOptions {
        private Boolean canBackorder; // Allow ordering when out of stock
        private Boolean canPreorder; // Allow pre-ordering for future delivery
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionUrls {
        private String details;
        private String compare;
    }
}
