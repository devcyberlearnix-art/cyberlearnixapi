package com.lms.coupon_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String code;

    private String title;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double minimumOrderAmount;
    private Double maximumDiscountAmount;

    private Double minPurchase;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiryDate;

    private Integer usageLimit;
    private Integer perUserLimit;

    @Builder.Default
    private Integer usedCount = 0;

    @Builder.Default
    @JsonProperty("isActive")
    private boolean isActive = true;

    private String status;
    private Boolean stackable;
    private String createdById;
    private String createdByRole;
    private String applicableTo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coupon_courses", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "course_id")
    private List<Long> courses;

    private String assignedUserId;
    private String campaignId;
}