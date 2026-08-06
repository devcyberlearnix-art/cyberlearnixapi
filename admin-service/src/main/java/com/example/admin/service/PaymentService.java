package com.example.admin.service;

import com.example.admin.dto.PaymentDto;
import com.example.admin.security.JwtService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${payment-service.url:http://localhost:8085/payments}")
    private String paymentServiceUrl;

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    private HttpEntity<Void> createEntity() {
        return new HttpEntity<>(null); // No auth headers since /payments is now public
    }

    public List<PaymentDto> getAllPayments() {
        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            paymentServiceUrl,
                            HttpMethod.GET,
                            createEntity(),
                            String.class
                    );
            System.out.println("Payment service response status: " + response.getStatusCode());
            System.out.println("Payment service response body: " + response.getBody());
            
            // Parse the response manually
            String body = response.getBody();
            if (body == null || body.trim().isEmpty() || body.equals("[]")) {
                return List.of();
            }
            List<PaymentDto> payments = objectMapper.readValue(body, new TypeReference<List<PaymentDto>>() {});
            return payments;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Return empty list instead of throwing exception
        }
    }
    public PaymentDto getPaymentById(UUID id) {

        String url = paymentServiceUrl + "/" + id;

        ResponseEntity<PaymentDto> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(), PaymentDto.class);
        return response.getBody();
    }
}