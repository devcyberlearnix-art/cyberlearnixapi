package com.cyberlearnix.commonlibs.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 import org.hibernate.annotations.NotFound;
 import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory")
public class Inventory {

    @EmbeddedId
    private InventoryKey id;

    @ManyToOne
    @MapsId("storeId")
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    private StoreEntity store;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
     @NotFound(action = NotFoundAction.IGNORE)
    private Product product;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0;

    @Column(name = "mrp")
    private Double mrp;

    @Column(name = "discount_percentage")
    private Double discount = 0.0;

    @Column(name = "in_stock")
    private Boolean inStock = true;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold =5;

    @Column(name = "max_stock_capacity")
    private Integer maxStockCapacity;

    @Column(name = "reorder_point")
    private Integer reorderPoint = 10;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity = 50;

    @Column(name = "available_for_delivery")
    private Boolean availableForDelivery = true;

    @Column(name = "available_for_pickup")
    private Boolean availableForPickup = true;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "offer_description")
    private String offerDescription;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "hsn_code", length = 50)
    private String hsnCode;

    @Column(name = "gst_rate")
    private Double gstRate;

    @Column(name = "igst_rate")
    private Double igstRate;

    @Column(name = "cgst_rate")
    private Double cgstRate;

    @Column(name = "sgst_rate")
    private Double sgstRate;
}
