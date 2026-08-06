package com.lms.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InvoiceDto {
    /** All monetary amounts in this invoice are in this currency. */
    private String currency;
    private String invoiceNumber;
    private String invoiceDate;
    private Merchant merchant;
    private Customer customer;
    private List<Item> items;
    private PaymentInfo payment;
    private RefundInfo refund;
    private Summary summary;

    @Data
    @Builder
    public static class Merchant {
        private String name;
        private String gstin;
        private String email;
    }

    @Data
    @Builder
    public static class Customer {
        private String name;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    public static class Item {
        private String name;
        private Integer qty;
        private Double price;
        private Double total;
    }

    @Data
    @Builder
    public static class PaymentInfo {
        private String gateway;
        private String mihpayid;
        private String status;
    }

    @Data
    @Builder
    public static class RefundInfo {
        private String request_id;
        /** SUCCESS, FAILURE, or PENDING when a refund exists. */
        private String status;
        private Double amount;
        private String reason;
    }

    @Data
    @Builder
    public static class Summary {
        private String currency;
        private Double subtotal;
        private Double gst;
        private Double grandTotal;
    }
}

