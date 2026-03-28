package com.cyberlearnix.commonlibs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InventoryRequestDTO {
    // Getters and Setters
    @Schema(description = "Product ID (or provide gtin)", example = "42", required = false)
    private Long productId;

    @Schema(description = "Store ID (or provide merchantId)", example = "3", required = false)
    private Long storeId;

    // Optional merchant identifier (UUID as string) used to resolve store when storeId is not provided
    @Schema(description = "Merchant ID (UUID) to resolve store when storeId is absent", example = "e48ed1a8-ef58-4488-a93c-1cc9b4ab6d7d")
    private String merchantId;

    @Schema(description = "Available quantity", example = "100", required = true)
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Schema(description = "Reserved quantity", example = "5")
    @Min(value = 0, message = "Reserved quantity cannot be negative")
    private Integer reservedQuantity = 0;

    @Schema(description = "Current price", example = "99.99", required = true)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    @Schema(description = "Maximum retail price", example = "129.99")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than 0")
    private Double mrp;

    // Optional GTIN support when productId is not provided (server may look it up)
    @Schema(description = "Product GTIN (optional alternative to productId)", example = "8901764032215")
    private String gtin;

    @Schema(description = "Discount percentage", example = "10.0")
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private Double discount = 0.0;

    @Schema(description = "Is item currently in stock", example = "true")
    private Boolean inStock = true;

    @Schema(description = "Minimum stock threshold for alerts", example = "10")
    @Min(value = 0, message = "Minimum stock threshold cannot be negative")
    private Integer minStockThreshold = 5;

    // Alias for low stock threshold (requesters may send lowStockThreshold)
    @Schema(description = "Alias of minStockThreshold", example = "10")
    private Integer lowStockThreshold;

    @Schema(description = "Maximum stock capacity", example = "500")
    @Min(value = 1, message = "Maximum stock capacity must be positive")
    private Integer maxStockCapacity;

    // Alias for max stock capacity (requesters may send maxStock)
    @Schema(description = "Alias of maxStockCapacity", example = "1000")
    private Integer maxStock;

    @Schema(description = "Reorder point", example = "20")
    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint = 10;

    @Schema(description = "Reorder quantity", example = "50")
    @Min(value = 1, message = "Reorder quantity must be positive")
    private Integer reorderQuantity = 50;

    @Schema(description = "Is item available for delivery", example = "true")
    private Boolean availableForDelivery = true;

    @Schema(description = "Is item available for pickup", example = "true")
    private Boolean availableForPickup = true;

    @Schema(description = "Storage location within store", example = "Aisle 3, Shelf B")
    @Size(max = 100, message = "Storage location cannot exceed 100 characters")
    private String storageLocation;

    @Schema(description = "Notes or additional information", example = "Refrigerated item")
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Schema(description = "Product expiry date", example = "2025-12-31")
    private LocalDate expiryDate;

    @Schema(description = "Offer description", example = "Save ₹10 on bulk purchase")
    @Size(max = 255, message = "Offer description cannot exceed 255 characters")
    private String offerDescription;

    @Schema(description = "Batch number", example = "BATCH001")
    @Size(max = 100, message = "Batch number cannot exceed 100 characters")
    private String batchNumber;

    @Schema(description = "HSN code for the product", example = "19059010")
    @Size(max = 50, message = "HSN code cannot exceed 50 characters")
    private String hsnCode;

    @Schema(description = "GST rate percentage", example = "18.0")
    @DecimalMin(value = "0.0", message = "GST rate cannot be negative")
    @DecimalMax(value = "100.0", message = "GST rate cannot exceed 100%")
    private Double gstRate;

    @Schema(description = "IGST rate percentage", example = "18.0")
    @DecimalMin(value = "0.0", message = "IGST rate cannot be negative")
    @DecimalMax(value = "100.0", message = "IGST rate cannot exceed 100%")
    private Double igstRate;

    @Schema(description = "CGST rate percentage", example = "9.0")
    @DecimalMin(value = "0.0", message = "CGST rate cannot be negative")
    @DecimalMax(value = "100.0", message = "CGST rate cannot exceed 100%")
    private Double cgstRate;

    @Schema(description = "SGST rate percentage", example = "9.0")
    @DecimalMin(value = "0.0", message = "SGST rate cannot be negative")
    @DecimalMax(value = "100.0", message = "SGST rate cannot exceed 100%")
    private Double sgstRate;

    @Schema(description = "Product status", example = "AVAILABLE")
    @Size(max = 20, message = "Status cannot exceed 20 characters")
    private String status = "AVAILABLE";

    // Constructors
    public InventoryRequestDTO() {}

    public InventoryRequestDTO(Long productId, Long storeId, Integer quantity, Double price) {
        this.productId = productId;
        this.storeId = storeId;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public String toString() {
        return "InventoryRequestDTO{" +
                "productId=" + productId +
                ", storeId=" + storeId +
                ", quantity=" + quantity +
                ", reservedQuantity=" + reservedQuantity +
                ", price=" + price +
                ", mrp=" + mrp +
                ", discountPercentage=" + discount +
                ", inStock=" + inStock +
                ", minStockThreshold=" + minStockThreshold +
                ", maxStockCapacity=" + maxStockCapacity +
                ", reorderPoint=" + reorderPoint +
                ", reorderQuantity=" + reorderQuantity +
                ", availableForDelivery=" + availableForDelivery +
                ", availableForPickup=" + availableForPickup +
                ", storageLocation='" + storageLocation + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
