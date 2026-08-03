package com.example.admin.service;

import com.example.admin.dto.OrderDto;
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

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${order-service.url:http://localhost:8084/orders}")
    private String orderServiceUrl;

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    private HttpEntity<Void> createEntity() {
        return new HttpEntity<>(null); // No auth headers since /orders is now public
    }

    public List<OrderDto> getAllOrders() {
        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            orderServiceUrl,
                            HttpMethod.GET,
                            createEntity(),
                            String.class
                    );
            System.out.println("Order service response status: " + response.getStatusCode());
            System.out.println("Order service response body: " + response.getBody());
            
            // Parse the response manually
            ObjectMapper mapper = new ObjectMapper();
            String body = response.getBody();
            if (body == null || body.trim().isEmpty() || body.equals("[]")) {
                return List.of();
            }
            List<OrderDto> orders = mapper.readValue(body, new TypeReference<List<OrderDto>>() {});
            return orders;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Return empty list instead of throwing exception
        }
    }

    public OrderDto getOrderById(String id) {
        String url = orderServiceUrl + "/" + id;
        ResponseEntity<OrderDto> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(), OrderDto.class);
        return response.getBody();
    }

    public OrderDto updateOrderStatus(String id, String status) {
        String url = orderServiceUrl + "/" + id + "/status?status=" + status;
        restTemplate.exchange(url, HttpMethod.PUT, createEntity(), String.class);
        return getOrderById(id);
    }

    public OrderDto processRefund(String id) {
        String url = orderServiceUrl + "/" + id + "/refund";
        try {
            restTemplate.exchange(url, HttpMethod.POST, createEntity(), String.class);
        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest e) {
            // Handle business rule violations (e.g. status not COMPLETED)
            throw new RuntimeException(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
        }
        return getOrderById(id);
    }
}
