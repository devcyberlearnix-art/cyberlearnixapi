package com.cyberlearnix.commonlibs.elastisearch;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.List;

@Data
@Document(indexName = "orders")
public class OrderSearchDocument {

    @Id
    private String id; // Composite key: storeId-orderId

    @Field(type = FieldType.Long)
    private Long orderId;

    @Field(type = FieldType.Keyword)
    private String orderNumber;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long storeId;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String customerName;

    @Field(type = FieldType.Keyword)
    private String customerPhone;

    @Field(type = FieldType.Keyword)
    private String customerEmail;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Field(type = FieldType.Double)
    private BigDecimal subtotal;

    @Field(type = FieldType.Double)
    private BigDecimal tax;

    @Field(type = FieldType.Double)
    private BigDecimal deliveryFee;

    @Field(type = FieldType.Double)
    private BigDecimal discount;

    @Field(type = FieldType.Double)
    private BigDecimal netTotalAmount;

    @Field(type = FieldType.Integer)
    private Integer totalItems;

    @Field(type = FieldType.Double)
    private Double totalWeight;

    @Field(type = FieldType.Text)
    private String specialInstructions;

    @Field(type = FieldType.Text)
    private String cancellationReason;

    @Field(type = FieldType.Keyword)
    private String overallFulfillmentStatus;

    // Refund fields
    @Field(type = FieldType.Double)
    private BigDecimal totalRefundAmount;

    @Field(type = FieldType.Double)
    private BigDecimal totalRefundGstAmount;

    @Field(type = FieldType.Double)
    private BigDecimal totalRefundIncludingGst;

    // Timestamps (stored as ISO strings to avoid serialization issues)
    @Field(type = FieldType.Keyword)
    private String orderDate;

    @Field(type = FieldType.Keyword)
    private String createdAt;

    @Field(type = FieldType.Keyword)
    private String updatedAt;

    @Field(type = FieldType.Keyword)
    private String confirmedAt;

    @Field(type = FieldType.Keyword)
    private String packedAt;

    @Field(type = FieldType.Keyword)
    private String shippedAt;

    @Field(type = FieldType.Keyword)
    private String deliveredAt;

    @Field(type = FieldType.Keyword)
    private String cancelledAt;

    // Denormalized order items
    @Field(type = FieldType.Nested)
    private List<OrderItemDocument> orderItems;

    // Searchable text from order items (concatenated product names for full-text search)
    @Field(type = FieldType.Text, analyzer = "standard")
    private String itemProductNames;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String itemBrands;

    @Data
    public static class OrderItemDocument {

        @Field(type = FieldType.Long)
        private Long orderItemId;

        @Field(type = FieldType.Long)
        private Long productId;

        @Field(type = FieldType.Text, analyzer = "standard")
        private String productName;

        @Field(type = FieldType.Keyword)
        private String brand;

        @Field(type = FieldType.Keyword)
        private String category;

        @Field(type = FieldType.Text)
        private String productDescription;

        @Field(type = FieldType.Text)
        private String productImageUrl;

        @Field(type = FieldType.Integer)
        private Integer quantity;

        @Field(type = FieldType.Double)
        private BigDecimal unitPrice;

        @Field(type = FieldType.Double)
        private BigDecimal totalPrice;

        @Field(type = FieldType.Keyword)
        private String unit;

        @Field(type = FieldType.Double)
        private Double unitWeight;

        @Field(type = FieldType.Double)
        private Double totalWeight;

        @Field(type = FieldType.Text)
        private String specialInstructions;

        // GST fields
        @Field(type = FieldType.Double)
        private BigDecimal gstAmount;

        @Field(type = FieldType.Double)
        private BigDecimal totalGstAmount;

        @Field(type = FieldType.Double)
        private BigDecimal totalPriceWithGst;
    }
}
