package com.lms.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InstructorCoursePaymentDto {
    private Long paymentRecordId;
    private String payuPaymentId;
    private String txnId;
    private String payerUserId;
    private String payerName;
    private String payerEmail;
    private String payerPhone;
    private Double amount;
    private String currency;
    private String paymentStatus;
    private String refundReason;
    /** PENDING, SUCCESS, or FAILURE — null if no refund was requested. */
    private String refundStatus;
    private Double refundAmount;
    private String refundRequestId;
    private LocalDateTime paidAt;
}
