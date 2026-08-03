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
public class AdminCouponServiceClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    @Value("${coupon-service.url:http://localhost:8082}")
    private String couponServiceUrl;

    public AdminCouponServiceClient(RestTemplate restTemplate, JwtService jwtService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
    }

    private HttpHeaders createHeaders() {
        return jwtService.createServiceAuthHeaders();
    }

    public List<CouponDTO> getAllCoupons() {
        try {
            String url = couponServiceUrl + "/api/v1/coupons";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            return parseCouponList(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get coupons from Coupon Service: " + e.getMessage());
            return List.of();
        }
    }

    public CouponDTO getCouponById(String couponId) {
        try {
            String url = couponServiceUrl + "/api/v1/coupons/" + couponId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Map.class
            );
            return mapToCouponDto(response.getBody());
        } catch (Exception e) {
            System.err.println("✗ Failed to get coupon from Coupon Service: " + e.getMessage());
            return null;
        }
    }

    public List<String> getCampaigns() {
        try {
            String url = couponServiceUrl + "/api/v1/coupons/campaigns";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    Object[].class
            );
            if (response.getBody() == null) {
                return List.of();
            }
            List<String> result = new ArrayList<>();
            for (Object item : response.getBody()) {
                result.add(String.valueOf(item));
            }
            return result;
        } catch (Exception e) {
            System.err.println("✗ Failed to get campaigns from Coupon Service: " + e.getMessage());
            return List.of();
        }
    }

    private List<CouponDTO> parseCouponList(Object[] body) {
        if (body == null) {
            return List.of();
        }
        List<CouponDTO> result = new ArrayList<>();
        for (Object item : body) {
            result.add(mapToCouponDto(item));
        }
        return result;
    }

    private CouponDTO mapToCouponDto(Object body) {
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

    private CouponDTO mapFromMap(Map<?, ?> map) {
        CouponDTO dto = new CouponDTO();
        dto.setId(getString(map.get("id")));
        dto.setCode(getString(map.get("code")));
        dto.setTitle(getString(map.get("title")));
        dto.setDescription(getString(map.get("description")));
        dto.setDiscountType(getString(map.get("discountType")));
        dto.setDiscountValue(getDouble(map.get("discountValue")));
        dto.setMinimumOrderAmount(getDouble(map.get("minimumOrderAmount")));
        dto.setMaximumDiscountAmount(getDouble(map.get("maximumDiscountAmount")));
        dto.setStartTime(getString(map.get("startTime")));
        dto.setEndTime(getString(map.get("endTime")));
        dto.setUsageLimit(getInteger(map.get("usageLimit")));
        dto.setUsedCount(getInteger(map.get("usedCount")));
        dto.setIsActive(getBoolean(map.get("isActive")));
        dto.setStatus(getString(map.get("status")));
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

    private Boolean getBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CouponDTO {
        private String id;
        private String code;
        private String title;
        private String description;
        private String discountType;
        private Double discountValue;
        private Double minimumOrderAmount;
        private Double maximumDiscountAmount;
        private String startTime;
        private String endTime;
        private Integer usageLimit;
        private Integer usedCount;
        private Boolean isActive;
        private String status;
    }
}
