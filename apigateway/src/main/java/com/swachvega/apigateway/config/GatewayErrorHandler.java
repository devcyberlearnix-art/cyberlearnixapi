package com.swachvega.apigateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swachvega.commonlibs.dto.ApiResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorHandler.class);

    private final ObjectMapper objectMapper;

    public GatewayErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = mapStatus(ex);

        // Keep response body consistent with your existing pattern
        Map<String, Object> meta = new HashMap<>();
        meta.put("error", status == HttpStatus.BAD_GATEWAY ? "BAD_GATEWAY"
                : status == HttpStatus.GATEWAY_TIMEOUT ? "GATEWAY_TIMEOUT"
                : "INTERNAL_ERROR");

        ApiResponseWrapper<Void> body = new ApiResponseWrapper<>(false, userMessage(status, ex), null, meta);

        byte[] bytes = serialize(body);

        if (status.is5xxServerError()) {
            log.error("Gateway request failed: status={} path={} cause={}", status.value(), exchange.getRequest().getURI(), ex.toString(), ex);
        } else {
            log.warn("Gateway request failed: status={} path={} cause={}", status.value(), exchange.getRequest().getURI(), ex.toString());
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private HttpStatus mapStatus(Throwable ex) {
        Throwable root = rootCause(ex);

        if (root instanceof UnknownHostException || root instanceof ConnectException) {
            return HttpStatus.BAD_GATEWAY;
        }

        // Netty/Reactive timeouts sometimes come as RuntimeException subclasses with these names
        String name = root.getClass().getName();
        if (name.contains("ReadTimeout") || name.contains("Timeout")) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String userMessage(HttpStatus status, Throwable ex) {
        if (status == HttpStatus.BAD_GATEWAY) {
            // Extract service name from exception or URL
            String serviceName = extractServiceName(ex);
            if (serviceName != null) {
                return String.format("Service '%s' is not running. Please start the service and try again.", serviceName);
            }
            return "Required service is not running. Please check if all services are started.";
        }
        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return "Service is taking too long to respond. Please try again later.";
        }
        return "Internal server error occurred. Please contact support if the issue persists.";
    }

    private String extractServiceName(Throwable ex) {
        // Try to extract service name from connection exception
        Throwable root = rootCause(ex);
        String message = root.getMessage();
        if (message != null) {
            // Common patterns: "Connection refused: merchantservice:8080"
            if (message.contains("merchantservice")) return "merchantservice (port 8089)";
            if (message.contains("userservice")) return "userservice (port 8086)";
            if (message.contains("productservice")) return "productservice (port 8082)";
            if (message.contains("orderservice")) return "orderservice (port 8081)";
            if (message.contains("searchservice")) return "searchservice (port 8085)";
            if (message.contains("cartservice")) return "cartservice (port 8087)";
            if (message.contains("inventoryservice")) return "inventoryservice (port 8083)";
            if (message.contains("storeservice")) return "storeservice (port 8084)";
            if (message.contains("couponservice")) return "couponservice (port 8090)";
            if (message.contains("chatservice")) return "chatservice (port 8091)";
            if (message.contains("adminservice")) return "adminservice (port 8088)";
            if (message.contains("notificationservice")) return "notificationservice (port 8092)";
            if (message.contains("landingpageservice")) return "landingpageservice (port 8093)";
            if (message.contains("ratingfavoriteservice")) return "ratingfavoriteservice (port 8094)";
        }
        return null;
    }

    private byte[] serialize(Object body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            // Extremely defensive fallback
            String s = "{\"success\":false,\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"error\":\"INTERNAL_ERROR\"}}";
            return s.getBytes(StandardCharsets.UTF_8);
        }
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }
}
