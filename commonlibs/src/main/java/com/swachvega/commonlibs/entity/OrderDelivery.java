package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "order_deliveries", indexes = {
        @Index(name = "idx_delivery_order_id", columnList = "orderId"),
        @Index(name = "idx_delivery_status", columnList = "status"),
        @Index(name = "idx_delivery_date", columnList = "estimatedDeliveryDate")
})
public class OrderDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long deliveryId;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryType type = DeliveryType.HOME_DELIVERY;

    // Customer delivery address (snapshot at time of order)
    @Column(nullable = false, length = 500)
    private String deliveryAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String zipCode;

    @Column(length = 20)
    private String customerPhone;

    @Column(length = 100)
    private String customerName;

    // External rider service confirmation ID (returned by POST /create-delivery)
    @Column(name = "delivery_confirmation_id", length = 150)
    private String deliveryConfirmationId;

    // Delivery partner information
    @Column(length = 100)
    private String deliveryPartnerName;

    @Column(length = 20)
    private String deliveryPartnerPhone;

    @Column(length = 100)
    private String vehicleNumber;

    @Column(length = 50)
    private String trackingNumber;

    // Timing information
    @Column
    private LocalDateTime estimatedPickupTime;

    @Column
    private LocalDateTime actualPickupTime;

    @Column
    private LocalDateTime estimatedDeliveryDate;

    @Column
    private LocalDateTime actualDeliveryDate;

    // Location tracking
    @Column
    private Double currentLatitude;

    @Column
    private Double currentLongitude;

    // Approximate distance between store and delivery address (km)
    @Column
    private Double distanceKm;

    @Column
    private LocalDateTime lastLocationUpdate;

    @Column(length = 500)
    private String deliveryInstructions;

    @Column(length = 500)
    private String deliveryNotes; // Notes from delivery partner

    @Column(length = 500)
    private String failureReason; // Reason if delivery failed

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // One-to-one relationship with Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable = false, updatable = false)
    private Order order;

    public enum DeliveryStatus {
        PENDING,            // Delivery not yet assigned
        ASSIGNED,           // Assigned to delivery partner
        PICKED_UP,          // Order picked up from store
        IN_TRANSIT,         // On the way to customer
        OUT_FOR_DELIVERY,   // Out for final delivery
        DELIVERED,          // Successfully delivered
        FAILED,             // Delivery failed
        RETURNED            // Returned to store
    }

    public enum DeliveryType {
        HOME_DELIVERY,      // Delivery to customer address
        STORE_PICKUP,       // Customer pickup from store
        CURBSIDE_PICKUP     // Customer pickup from store parking
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    public void assign(String partnerName, String partnerPhone, String vehicleNumber) {
        this.deliveryPartnerName = partnerName;
        this.deliveryPartnerPhone = partnerPhone;
        this.vehicleNumber = vehicleNumber;
        this.status = DeliveryStatus.ASSIGNED;
    }

    public void markPickedUp() {
        if (status != DeliveryStatus.ASSIGNED) {
            throw new IllegalStateException("Delivery can only be picked up from ASSIGNED status");
        }
        this.status = DeliveryStatus.PICKED_UP;
        this.actualPickupTime = LocalDateTime.now();
    }

    public void markInTransit() {
        if (status != DeliveryStatus.PICKED_UP) {
            throw new IllegalStateException("Delivery can only be in transit from PICKED_UP status");
        }
        this.status = DeliveryStatus.IN_TRANSIT;
    }

    public void markOutForDelivery() {
        if (status != DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException("Delivery can only be out for delivery from IN_TRANSIT status");
        }
        this.status = DeliveryStatus.OUT_FOR_DELIVERY;
    }

    public void markDelivered(String notes) {
        if (status != DeliveryStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("Delivery can only be completed from OUT_FOR_DELIVERY status");
        }
        this.status = DeliveryStatus.DELIVERED;
        this.actualDeliveryDate = LocalDateTime.now();
        this.deliveryNotes = notes;
    }

    public void markFailed(String reason) {
        this.status = DeliveryStatus.FAILED;
        this.failureReason = reason;
    }

    public void updateLocation(Double latitude, Double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.lastLocationUpdate = LocalDateTime.now();
    }
}
