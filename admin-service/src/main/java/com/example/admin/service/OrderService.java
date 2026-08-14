package com.example.admin.service;

import com.example.admin.dto.OrderDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${order-service.url:http://localhost:8084/orders}")
    private String orderServiceUrl;

    private HttpEntity<Void> createEntity(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        return new HttpEntity<>(headers);
    }

    public List<OrderDto> getAllOrders(String authorization) {
        ResponseEntity<Map> response = restTemplate.exchange(
                orderServiceUrl + "/api/v1/orders/admin",
                HttpMethod.GET,
                createEntity(authorization),
                Map.class);
        Object data = response.getBody() != null ? response.getBody().get("data") : null;
        if (data == null) {
            return List.of();
        }
        return objectMapper.convertValue(data,
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDto.class));
    }

    public OrderDto getOrderById(String id, String authorization) {
        String url = orderServiceUrl + "/api/v1/orders/" + id;
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(authorization), Map.class);
        return extractOrder(response.getBody());
    }

    public OrderDto updateOrderStatus(String id, String status, String authorization) {
        String url = orderServiceUrl + "/api/v1/orders/" + id + "/status?status=" + status;
        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.PUT, createEntity(authorization), Map.class);
        return extractOrder(response.getBody());
    }

    public OrderDto processRefund(String id, String authorization) {
        String url = orderServiceUrl + "/api/v1/orders/" + id + "/refund";
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, createEntity(authorization), Map.class);
            return extractOrder(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest e) {
            // Handle business rule violations (e.g. status not COMPLETED)
            throw new RuntimeException(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
        }
    }

    private OrderDto extractOrder(Map<?, ?> response) {
        Object data = response != null ? response.get("data") : null;
        return data == null ? null : objectMapper.convertValue(data, OrderDto.class);
    }
}
