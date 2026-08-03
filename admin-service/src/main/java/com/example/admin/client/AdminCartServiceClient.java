package com.example.admin.client;

import com.example.admin.security.JwtService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AdminCartServiceClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${cart-service.url:http://localhost:8081}")
    private String cartServiceUrl;

    public AdminCartServiceClient(RestTemplate restTemplate, JwtService jwtService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
    }

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    public List<CartDTO> getAllCarts() {
        try {
            String url = cartServiceUrl + "/api/v1/cart/all";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseCartList(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get carts from Cart Service: " + e.getMessage());
            return List.of();
        }
    }

    public CartDTO getCartByUserId(String userId) {
        try {
            String url = cartServiceUrl + "/api/v1/cart/user/" + userId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToCartDto(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get cart from Cart Service: " + e.getMessage());
            return null;
        }
    }

    private List<CartDTO> parseCartList(Object[] body) {
        if (body == null) {
            return List.of();
        }
        List<CartDTO> result = new ArrayList<>();
        for (Object item : body) {
            result.add(mapToCartDto(item));
        }
        return result;
    }

    private CartDTO mapToCartDto(Object body) {
        if (body == null) {
            return null;
        }
        if (body instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                return mapFromMap(dataMap);
            }
            return mapFromMap(map);
        }
        return null;
    }

    private CartDTO mapFromMap(Map<?, ?> map) {
        CartDTO dto = new CartDTO();
        dto.setUserId(getString(map.get("userId")));
        dto.setTotalAmount(getDouble(map.get("totalAmount")));
        dto.setDiscountAmount(getDouble(map.get("discountAmount")));
        dto.setFinalAmount(getDouble(map.get("finalAmount")));
        dto.setItemCount(getInteger(map.get("itemCount")));
        return dto;
    }

    private String getString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Double getDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CartDTO {
        private String userId;
        private Double totalAmount;
        private Double discountAmount;
        private Double finalAmount;
        private Integer itemCount;
    }
}
