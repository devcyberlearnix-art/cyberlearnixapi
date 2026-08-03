package com.lms.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentRequest {
    /** Required; amount and productInfo are loaded from this course. */
    private String courseId;
    private String firstName;
    private String email;
    private String phone;
    /** Learner user id who is paying. */
    private String payerUserId;
    /** Defaults to INR when omitted. */
    private String currency;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Double amount;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String productInfo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String instructorId;
}
