package com.lms.paymentservice.dto;

import lombok.Data;

@Data
public class PayUConsentCheckoutRequest {
    private String key;
    private String txnid;
    private String amount;
    private String productinfo;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String surl;
    private String furl;

    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String zipcode;

    private String udf1;
    private String udf2;
    private String udf3;
    private String udf4;
    private String udf5;

    private String api_version; // e.g. "7"
    private String si;          // e.g. "1"
    private String si_details;  // JSON string
}

