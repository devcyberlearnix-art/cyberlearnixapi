package com.swachvega.apigateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
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

        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = java.util.UUID.randomUUID().toString();
        }

        String code = status == HttpStatus.BAD_GATEWAY ? "BAD_GATEWAY"
                : status == HttpStatus.GATEWAY_TIMEOUT ? "GATEWAY_TIMEOUT"
                : status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND"
                : status == HttpStatus.METHOD_NOT_ALLOWED ? "METHOD_NOT_ALLOWED"
                : status == HttpStatus.BAD_REQUEST ? "BAD_REQUEST"
                : status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED"
                : status == HttpStatus.FORBIDDEN ? "FORBIDDEN"
                : "INTERNAL_SERVER_ERROR";

        String message = userMessage(status, ex);
        String path = exchange.getRequest().getURI().getPath();

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("timestamp", java.time.Instant.now().toString());
        body.put("status", status.value());
        body.put("code", code);
        body.put("message", message);
        body.put("path", path);
        body.put("traceId", traceId);

        byte[] bytes = serialize(body);

        if (status.is5xxServerError()) {
            log.error("[traceId={}] Gateway request failed: status={} code={} path={} cause={}",
                    traceId, status.value(), code, path, ex.toString(), ex);
        } else {
            log.warn("[traceId={}] Gateway request failed: status={} code={} path={} cause={}",
                    traceId, status.value(), code, path, ex.toString());
        }

        try {
            response.setStatusCode(status);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().set("X-Trace-Id", traceId);
        } catch (UnsupportedOperationException ignored) {
        }

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private HttpStatus mapStatus(Throwable ex) {
        Throwable root = rootCause(ex);

        if (root instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        if (root instanceof WebClientResponseException wcre) {
            return HttpStatus.valueOf(wcre.getStatusCode().value());
        }
        if (root instanceof NoResourceFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (root instanceof UnknownHostException || root instanceof ConnectException) {
            return HttpStatus.BAD_GATEWAY;
        }

        String name = root.getClass().getName();
        if (name.contains("ReadTimeout") || name.contains("Timeout")) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String userMessage(HttpStatus status, Throwable ex) {
        Throwable root = rootCause(ex);
        if (status == HttpStatus.BAD_GATEWAY) {
            return "Downstream service is temporarily unavailable. Please try again later.";
        }
        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return "Service is taking too long to respond. Please try again later.";
        }
        if (status == HttpStatus.NOT_FOUND) {
            return "Requested API path was not found on gateway or downstream service.";
        }
        if (status == HttpStatus.METHOD_NOT_ALLOWED) {
            return "HTTP method not supported for this endpoint.";
        }
        if (status == HttpStatus.BAD_REQUEST) {
            if (root instanceof ResponseStatusException rse && rse.getReason() != null) {
                return rse.getReason();
            }
            return "Invalid request parameters or headers.";
        }
        if (status == HttpStatus.UNAUTHORIZED) {
            return "Authentication required or invalid credentials.";
        }
        if (status == HttpStatus.FORBIDDEN) {
            return "Access denied: insufficient permissions.";
        }
        return "Internal server error occurred. Please contact support if the issue persists.";
    }

    private byte[] serialize(Object body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            String s = "{\"status\":500,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"Internal server error occurred.\"}";
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
