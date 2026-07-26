package com.lms.paymentservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.paymentservice.dto.PaymentLinkRequest;
import com.lms.paymentservice.dto.PaymentLinkResponse;
import com.lms.paymentservice.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PayUPaymentLinkService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${payu.payment-links.base-url:https://uatoneapi.payu.in}")
    private String baseUrl;

    public PaymentLinkResponse createPaymentLink(String merchantId, String accessToken, PaymentLinkRequest requestBody) {
        if (merchantId == null || merchantId.isBlank()) {
            throw new BadRequestException("merchantId is required");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new BadRequestException("accessToken is required");
        }
        if (requestBody == null) {
            throw new BadRequestException("request body is required");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("merchantId", merchantId);
        headers.setBearerAuth(accessToken);

        HttpEntity<PaymentLinkRequest> request = new HttpEntity<>(requestBody, headers);
        String url = baseUrl + "/payment-links/";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (RestClientException ex) {
            throw new BadRequestException("PayU payment-links request failed: " + ex.getMessage());
        }

        String body = response.getBody();
        if (body == null || body.isBlank()) {
            throw new BadRequestException("Empty response from PayU payment-links");
        }

        try {
            return objectMapper.readValue(body, new TypeReference<PaymentLinkResponse>() {});
        } catch (Exception ex) {
            throw new BadRequestException("PayU payment-links returned malformed JSON: " + ex.getMessage());
        }
    }
}

