package com.swachvega.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TraceIdGlobalFilter.class);
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank() || traceId.length() > 64) {
            traceId = UUID.randomUUID().toString();
        }

        final String finalTraceId = traceId;
        HttpHeaders safeHeaders = new HttpHeaders();
        exchange.getRequest().getHeaders().forEach(safeHeaders::addAll);
        safeHeaders.set(TRACE_ID_HEADER, finalTraceId);

        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return safeHeaders;
            }
        };

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        return chain.filter(mutatedExchange).doFinally(signalType -> {
            long duration = System.currentTimeMillis() - startTime;
            var statusCode = exchange.getResponse().getStatusCode();
            int status = statusCode != null ? statusCode.value() : 500;
            if (status >= 500) {
                log.error("[traceId={}] Gateway completed {} {} with status {} in {}ms", finalTraceId, method, path, status, duration);
            } else if (status >= 400) {
                log.warn("[traceId={}] Gateway completed {} {} with status {} in {}ms", finalTraceId, method, path, status, duration);
            } else {
                log.info("[traceId={}] Gateway completed {} {} with status {} in {}ms", finalTraceId, method, path, status, duration);
            }
        });
    }

    @Override
    public int getOrder() {
        return -200; // Run before JwtAuthenticationFilter (-100)
    }
}
