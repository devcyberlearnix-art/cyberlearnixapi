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
public class AdminWishlistServiceClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${wishlist-service.url:http://localhost:8090}")
    private String wishlistServiceUrl;

    public AdminWishlistServiceClient(RestTemplate restTemplate, JwtService jwtService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
    }

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    public List<WishlistDTO> getAllWishlists() {
        try {
            String url = wishlistServiceUrl + "/api/v1/wishlist/all";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseWishlistList(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get wishlists from Wishlist Service: " + e.getMessage());
            return List.of();
        }
    }

    public WishlistDTO getWishlistByUserId(String userId) {
        try {
            String url = wishlistServiceUrl + "/api/v1/wishlist/user/" + userId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToWishlistDto(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get wishlist from Wishlist Service: " + e.getMessage());
            return null;
        }
    }

    private List<WishlistDTO> parseWishlistList(Object[] body) {
        if (body == null) {
            return List.of();
        }
        List<WishlistDTO> result = new ArrayList<>();
        for (Object item : body) {
            result.add(mapToWishlistDto(item));
        }
        return result;
    }

    private WishlistDTO mapToWishlistDto(Object body) {
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

    private WishlistDTO mapFromMap(Map<?, ?> map) {
        WishlistDTO dto = new WishlistDTO();
        dto.setUserId(getString(map.get("userId")));
        dto.setItemCount(getInteger(map.get("itemCount")));
        dto.setCreatedAt(getString(map.get("createdAt")));
        return dto;
    }

    private String getString(Object value) {
        return value == null ? null : String.valueOf(value);
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
    public static class WishlistDTO {
        private String userId;
        private Integer itemCount;
        private String createdAt;
    }
}
